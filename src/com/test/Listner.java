package com.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author mkjodhani
 * @project
 * @since 18/03/23
 */
public class Listner {
    public static void main(String[] args) throws IOException {
        int port = Integer.valueOf(args[0]);
        DatagramSocket socket = new DatagramSocket(port);
        byte[] array = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
        socket.receive(datagramPacket);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                        try {
                            socket.send(
                                    new DatagramPacket(String.valueOf(port).getBytes(), String.valueOf(port).length(),
                                            datagramPacket.getAddress(), datagramPacket.getPort()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                (port - 3000) * 1000);
    }
}
