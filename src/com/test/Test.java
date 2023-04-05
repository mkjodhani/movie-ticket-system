package com.test;

import com.frontend.registry.CentralRepository;
import com.helper.Commands;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

/**
 * @author mkjodhani
 * @project
 * @since 17/03/23
 */
public class Test {
    public static void main(String[] args) throws IOException {
        String initCommand = Commands.getInitReplicaCommand(3001);
        DatagramSocket socket = new DatagramSocket(3001);
        socket.send(
                new DatagramPacket(initCommand.getBytes(), initCommand.length(), Inet4Address.getLocalHost(), 3002));
        byte[] replyBytes = new byte[1024];
        DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length);
        socket.receive(replyPacket);
        String reply = new String(replyPacket.getData(), 0, replyPacket.getLength());
        // TODO handle AKG message
        System.out.println(reply);
    }
}
