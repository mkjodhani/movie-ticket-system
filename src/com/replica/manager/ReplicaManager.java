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
    private static boolean enabledDelay = false;
    private static DatagramSocket rmUDPSocket = null;
    public static HashMap<String, TheatreMetaData> locationPorts = new HashMap<>();
    public static Theatre atwThread,verThread,outThread;
    public static void main(String[] args) throws UnknownHostException {
        // TODO Move this to Replica Manager for fault tolerance
        initManager();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // if the replica is not running
                if (!(atwThread.isAlive() && verThread.isAlive() && outThread.isAlive())){
                    return;
                }
//                else send the heart beat
                try {
                    String request  = Commands.getHeartBeatCommand(Config.rm1Port);
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(request.getBytes(),request.length(),Inet4Address.getByName(Config.frontEndAddress),Config.frontendHeartBeatSocket);
                    socket.send(packet);
                    byte[] receivedBytes = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBytes,receivedBytes.length);
                    socket.receive(receivedPacket);
                    String response = new String(receivedBytes,0,receivedPacket.getLength());
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
        scheduledThreadPool.scheduleAtFixedRate(timerTask, 1, 5, TimeUnit.SECONDS);
        startUDPListener();
//        sendReadyToExecute();
    }
    private static void sendReadyToExecute() {
        try {
            String request = Commands.getReadyToExecuteCommand(Config.rm1Port);
            System.out.println("sendReadyToExecute::"+request);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(request.getBytes(),request.length(),Inet4Address.getByName(Config.frontEndAddress),Config.frontendHeartBeatSocket);
            socket.send(packet);
            byte[] receivedBytes = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(receivedBytes,receivedBytes.length);
            socket.receive(receivedPacket);
            String response = new String(receivedBytes,0,receivedPacket.getLength());
            System.out.println("sendReadyToExecute::RES::"+response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void sendGetListOfRequest() {
        try {
            String request = Commands.getGetListOfRequestsCommand();
            System.out.println("sendGetListOfRequest::"+request);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(request.getBytes(),request.length(),Inet4Address.getByName(Config.frontEndAddress),Config.frontendHeartBeatSocket);
            socket.send(packet);
            byte[] receivedBytes = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(receivedBytes,receivedBytes.length);
            System.out.println("receive:1");
            socket.receive(receivedPacket);
            System.out.println("receive:2");
            String response = new String(receivedBytes,0,receivedPacket.getLength());
            for (String command:response.split(System.lineSeparator())){
                String result = handleQuery(command);
                System.out.println("handleQuery(command):::"+result);
            }
            System.out.println("sendGetListOfRequest::RES::"+response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static String handleQuery(String commandQuery) throws UnknownHostException {
        String query = commandQuery.toLowerCase();
        System.out.println("handleQuery::1234:"+query);
        String command = Commands.generateParamsFromCommand(query)[1].toUpperCase();
        String id = Commands.generateParamsFromCommand(query)[0].toUpperCase();
        Admin admin;
        Customer customer;
        String response = "TEMP";
        String movieID,movieName,bookingCapacity,customerID,numberOfTickets;
        System.out.println("command::"+command);
        switch (command){
            case Commands.RESTART_SERVER_REPLICA:
                System.out.println("RESTART_SERVER_REPLICA");
                response = Commands.GET_LIST_OF_REQUESTS;
                break;
            case Commands.GET_LIST_OF_REQUESTS:
                String commands = query.replace(id+Commands.DELIMITER,"").replace(Commands.GET_LIST_OF_REQUESTS+Commands.DELIMITER,"");
                System.out.println("----------------");
                System.out.println(commands);
                System.out.println("----------------");
                response = Commands.READY_TO_EXECUTE;
                break;
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
            default:
                System.out.println("command::--::command::"+command);
        }
        System.out.println(response);
        return response;
    }
    public static void initManager() throws UnknownHostException {
        locationPorts = new HashMap<>();
        locationPorts.put("atw",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Atwater", "atw", 5051));
        locationPorts.put("ver",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Verdun", "ver", 5052));
        locationPorts.put("out",
                new TheatreMetaData(InetAddress.getLocalHost().getHostAddress(), "Outremont", "out", 5053));

        TheatreMetaData atwaterInfo = locationPorts.get("atw");
        atwThread = new Theatre(atwaterInfo.getPort(), atwaterInfo.getLocation(), atwaterInfo.getPrefix());
        atwThread.start();

        TheatreMetaData verdunInfo = locationPorts.get("ver");
        verThread = new Theatre(verdunInfo.getPort(), verdunInfo.getLocation(), verdunInfo.getPrefix());
        verThread.start();

        TheatreMetaData outremontInfo = locationPorts.get("out");
        outThread = new Theatre(outremontInfo.getPort(), outremontInfo.getLocation(),
                outremontInfo.getPrefix());
        outThread.start();
        System.out.println("STARTED");
    }
    public static void startUDPListener() {
        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    rmUDPSocket = new DatagramSocket(Config.rm1Port);
                    while (true) {
                        try {
                            System.out.println("UDP IS WAITING AT RM");
                            byte[] requestBytes = new byte[1024];
                            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
                            rmUDPSocket.receive(requestPacket);
                            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                            System.out.println("request:"+request);
                            if (request.contains(Commands.RESTART_SERVER_REPLICA)){
                                System.out.println("RESTART_SERVER_REPLICA::captured");
                                atwThread.interrupt();
                                atwThread.stopTheatre();
                                verThread.interrupt();
                                verThread.stopTheatre();
                                outThread.interrupt();
                                outThread.stopTheatre();
                                rmUDPSocket.close();
                                initManager();
                                startUDPListener();
                                Timer timer = new Timer();
                                int delay = 1000; // Delay in milliseconds (5 seconds)
                                TimerTask task = new TimerTask() {
                                    public void run() {
                                        System.out.println("SERc------------------------------------");
                                        sendGetListOfRequest();
                                    }
                                };

                                timer.schedule(task, delay);
//                                sendReadyToExecute();
                            }
                            else {
                                String id = Commands.generateParamsFromCommand(request)[0];
                                String response = Commands.generateCommandFromParams(new String[]{id,handleQuery(request)});
                                System.out.println("sending back response:"+response);
                                DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.length(),
                                        requestPacket.getAddress(), Config.frontendUDPPort);
//                                if (enabledDelay){
//                                    enabledDelay= !enabledDelay;
//                                }
//                                else {
                                    rmUDPSocket.send(sendPacket);
//                                    enabledDelay= !enabledDelay;
//                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
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
