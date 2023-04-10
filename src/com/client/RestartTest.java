package com.client;

import com.frontend.Frontend;
import com.helper.Commands;
import com.helper.Config;

import java.io.IOException;
import java.net.*;

/**
 * @author mkjodhani
 * @project
 * @since 09/04/23
 */
public class RestartTest {
    public static void main(String[] args) throws IOException {
        for (String hostAddress:Config.replicas){
            String restartCommand ="0,"+Commands.getRestartReplicaCommand(hostAddress, 3001);
            DatagramPacket datagramPacket = new DatagramPacket(restartCommand.getBytes(), restartCommand.length(),
                    Inet4Address.getByName(hostAddress), 3001);
            DatagramSocket socket = new DatagramSocket();
            socket.send(datagramPacket);
        }
    }
}
