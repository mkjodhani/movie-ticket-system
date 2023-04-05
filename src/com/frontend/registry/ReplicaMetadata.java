package com.frontend.registry;

import com.helper.Commands;

import java.net.*;

/**
 * @author mkjodhani
 * @version 1.0
 * @project Movie Ticket System
 * @since 17/03/23
 */
public class ReplicaMetadata {
    private int totalReplica = 0;
    String hostAddress;
    int port, id, lifeline;
    boolean active;

    public ReplicaMetadata(String hostAddress, int port, boolean active) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.id = ++totalReplica;
        this.active = active;
        this.lifeline = 3;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLifeline() {
        return lifeline;
    }

    public void executeReset() {
        ReplicaMetadata metadata = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String resetCommand = Commands.getRestartReplicaCommand(metadata.getHostAddress(),
                            metadata.getPort());
                    DatagramSocket socket = CentralRepository.getCentralRepository().getFrontEndSocket();
                    DatagramPacket requestPacket = new DatagramPacket(resetCommand.getBytes(), resetCommand.length(),
                            Inet4Address.getByName(metadata.getHostAddress()), metadata.getPort());
                    socket.send(requestPacket);
                    byte[] replyBytes = new byte[1024];
                    DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length);
                    socket.receive(replyPacket);
                    String reply = new String(replyPacket.getData(), 0, replyPacket.getLength());
                    // TODO handle AKG message
                    System.out.println(reply);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
