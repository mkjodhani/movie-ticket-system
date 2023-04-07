package com.replica.manager;

import com.helper.Commands;
import com.replica.theatre.Theatre;
import com.replica.theatre.TheatreMetaData;
import com.shared.Config;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

/**
 * @author mkjodhani
 * @project
 * @since 28/03/23
 */
public class ReplicaManager {
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
        startUDPListener();
    }
    private static String handleQuery(String query){
        System.out.println(query);
        String command = Commands.generateParamsFromCommand(query)[1];
        switch (command){
            case Commands.INT_SERVER:
                break;
            case Commands.AKG_INIT_SERVER:
                break;
            case Commands.RESTART_SERVER_REPLICA:
                break;
            case Commands.ADD_MOVIE_SLOT:
                break;
            case Commands.REMOVE_MOVIE_SLOT:
                break;
            case Commands.LIST_MOVIE_AVAILABILITY:
                break;
            case Commands.BOOK_MOVIE_TICKET:
                break;
            case Commands.GET_CUSTOMER_SCHEDULE:
                break;
            case Commands.CANCEL_MOVIE_TICKET:
                break;
            case Commands.GET_AVAILABLE_SEATS_BY_SLOT_ID:
                break;
            case Commands.EXCHANGE_BOOKED_TICKET:
                break;
            case Commands.GET_CX_BOOKED_SEATS_FOR_SLOT_ID:
                break;
        }
        return query.replace(Commands.generateParamsFromCommand(query)[0]+Commands.DELIMITER,"SUCCESS");
    }
    public static void startUDPListener() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                DatagramSocket datagramSocket = null;
                try {
                    datagramSocket = new DatagramSocket(Config.rm1Port);
                    while (true) {
                        try {
                            System.out.println("Listening");
                            byte[] requestBytes = new byte[1024];
                            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
                            datagramSocket.receive(requestPacket);
                            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                            System.out.println("request:"+request);
                            String response = handleQuery(request);
                            System.out.println("response:"+response);
                            DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.length(),
                                    requestPacket.getAddress(), Config.frontendUDPPort);
                            datagramSocket.send(sendPacket);
                            System.out.println("Sent");
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
}
