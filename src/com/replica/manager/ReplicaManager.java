package com.replica.manager;

import com.helper.Commands;
import com.replica.theatre.Theatre;
import com.replica.theatre.TheatreMetaData;

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
    public static void startUDPListener() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        DatagramSocket datagramSocket = new DatagramSocket(3001);
                        byte[] replyBytes = new byte[1024];
                        DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length);
                        datagramSocket.receive(replyPacket);
                        String reply = new String(replyPacket.getData(), 0, replyPacket.getLength());
                        System.out.println(reply);
                        DatagramPacket sendPacket = new DatagramPacket("initResponse".getBytes(), "initResponse".length(),
                                replyPacket.getAddress(), replyPacket.getPort());
                        datagramSocket.send(sendPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        thread.start();
    }
}
