package com.replica.manager;

import com.helper.Commands;
import com.helper.Role;
import com.replica.theatre.Theatre;
import com.replica.theatre.TheatreMetaData;
import com.shared.Admin;
import com.helper.Config;
import com.shared.Customer;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author mkjodhani
 * @project
 * @since 28/03/23
 */
public class ReplicaManager {
    private static DatagramSocket rmUDPSocket = null;
    public static final HashMap<String, TheatreMetaData> locationPorts = new HashMap<>();
    public static void main(String[] args) throws UnknownHostException, SocketException {
        // TODO Move this to Replica Manager for fault tolerance

        locationPorts.put("atw",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Atwater", "atw", 5051));
        locationPorts.put("ver",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Verdun", "ver", 5052));
        locationPorts.put("out",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Outremont", "out", 5053));

        TheatreMetaData atwaterInfo = locationPorts.get("atw");
        Theatre atwaterTheatre = new Theatre(atwaterInfo.getPort(), atwaterInfo.getLocation(), atwaterInfo.getPrefix());
        Thread atwThread = new Thread(atwaterTheatre);
        atwThread.start();

        TheatreMetaData verdunInfo = locationPorts.get("ver");
        Theatre verdunTheatre = new Theatre(verdunInfo.getPort(), verdunInfo.getLocation(), verdunInfo.getPrefix());
        Thread verThread = new Thread(verdunTheatre);
        verThread.start();

        TheatreMetaData outremontInfo = locationPorts.get("out");
        Theatre outremontTheatre = new Theatre(outremontInfo.getPort(), outremontInfo.getLocation(),
                outremontInfo.getPrefix());
        Thread outThread = new Thread(outremontTheatre);
        outThread.start();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String request  = Commands.getHeartBeatCommand(Config.rm1Port);
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(request.getBytes(),request.length(),Inet4Address.getByName(Config.frontEndAddress),Config.frontendHeartBeatSocket);
                    socket.send(packet);
                    byte[] receivedBytes = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBytes,receivedBytes.length);
                    socket.receive(receivedPacket);
                    String response = new String(receivedBytes,0,receivedPacket.getLength());
                    System.out.println(response);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(timerTask, 0, 5, TimeUnit.SECONDS);
        startUDPListener();
    }
    private static String handleQuery(String query){
        System.out.println(query);
        String command = Commands.generateParamsFromCommand(query)[1];
        Admin admin;
        Customer customer;
        String response = Commands.SEND_ERROR;
        String movieID,movieName,bookingCapacity,customerID,numberOfTickets;
        switch (command){
            case Commands.ADD_MOVIE_SLOT:
                movieID = Commands.generateParamsFromCommand(query)[2];
                movieName = Commands.generateParamsFromCommand(query)[3];
                bookingCapacity=Commands.generateParamsFromCommand(query)[4];
                admin = getAdmin(Role.getLocationPrefix(movieID));
                response = admin.addMovieSlots(movieID,movieName,Integer.parseInt(bookingCapacity));
                break;
            case Commands.REMOVE_MOVIE_SLOT:
                movieID = Commands.generateParamsFromCommand(query)[2];
                movieName=Commands.generateParamsFromCommand(query)[3];
                admin = getAdmin(Role.getLocationPrefix(movieID));
                response = admin.removeMovieSlots(movieID,movieName);
                break;
            case Commands.LIST_MOVIE_AVAILABILITY:
                admin = getAdmin((String) locationPorts.keySet().toArray()[0]);
                movieName=Commands.generateParamsFromCommand(query)[2];
                response = admin.listMovieShowsAvailability(movieName);
                break;
            case Commands.BOOK_MOVIE_TICKET:
                customerID = Commands.generateParamsFromCommand(query)[2];
                movieID = Commands.generateParamsFromCommand(query)[3];
                movieName = Commands.generateParamsFromCommand(query)[4];
                numberOfTickets = Commands.generateParamsFromCommand(query)[5];
                customer = getCustomer(Role.getLocationPrefix(customerID));
                response = customer.bookMovieTickets(customerID, movieID,movieName,Integer.valueOf(numberOfTickets));
                break;
            case Commands.GET_CUSTOMER_SCHEDULE:
                customerID = Commands.generateParamsFromCommand(query)[2];
                customer = getCustomer(Role.getLocationPrefix(customerID));
                response = customer.getBookingSchedule(customerID);
                break;
            case Commands.CANCEL_MOVIE_TICKET:
                customerID = Commands.generateParamsFromCommand(query)[2];
                movieID = Commands.generateParamsFromCommand(query)[3];
                movieName = Commands.generateParamsFromCommand(query)[4];
                numberOfTickets = Commands.generateParamsFromCommand(query)[5];
                customer = getCustomer(Role.getLocationPrefix(customerID));
                response = customer.cancelMovieTickets(customerID,movieID,movieName,Integer.valueOf(numberOfTickets));
                break;
            case Commands.EXCHANGE_BOOKED_TICKET:
                customerID = Commands.generateParamsFromCommand(query)[2];
                String oldMovieName = Commands.generateParamsFromCommand(query)[3];
                movieID = Commands.generateParamsFromCommand(query)[4];
                String newMovieID = Commands.generateParamsFromCommand(query)[5];
                String newMovieName = Commands.generateParamsFromCommand(query)[6];
                numberOfTickets = Commands.generateParamsFromCommand(query)[7 ];
                customer = getCustomer(Role.getLocationPrefix(customerID));
                response = customer.exchangeTickets(customerID,oldMovieName,movieID,newMovieID,newMovieName,Integer.valueOf(numberOfTickets));
                break;
        }
        return response;
    }
    public static void startUDPListener() {
        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    rmUDPSocket = new DatagramSocket(Config.rm1Port);
                    while (true) {
                        try {
                            byte[] requestBytes = new byte[1024];
                            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
                            rmUDPSocket.receive(requestPacket);
                            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                            System.out.println(request);
                            String id = Commands.generateParamsFromCommand(request)[0];
                            String response = Commands.generateCommandFromParams(new String[]{id,handleQuery(request)});
                            DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.length(),
                                    requestPacket.getAddress(), Config.frontendUDPPort);
                            rmUDPSocket.send(sendPacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();
    }
    public static Customer getCustomer(String locationPrefix) {
        try {
            return locationPorts.get(locationPrefix).getCustomer();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Admin getAdmin(String locationPrefix) {
        try {
            System.out.println("locationPrefix:"+locationPrefix);
            System.out.println(locationPorts.get(locationPrefix));
            System.out.println(locationPorts.get(locationPrefix).getAdmin());
            return locationPorts.get(locationPrefix).getAdmin();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
