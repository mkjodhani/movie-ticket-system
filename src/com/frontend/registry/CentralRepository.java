package com.frontend.registry;

import com.helper.Commands;
import com.shared.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.HashMap;

/**
 * @author mkjodhani
 * @project
 * @since 11/03/23
 */
public class CentralRepository {
    private HashMap<Integer, ReplicaMetadata> replicaServers;
    private DatagramSocket frontEndSocket = null;
    private static CentralRepository centralRepository;

    private CentralRepository() throws SocketException {
        replicaServers = new HashMap<>();
//        frontEndSocket = new DatagramSocket(Config.FrontendPort);
    }

    public static CentralRepository getCentralRepository() throws SocketException {
        if (centralRepository == null) {
            centralRepository = new CentralRepository();
        }
        return centralRepository;
    }

    public ReplicaMetadata getReplicaServer(int replicaID) {
        return replicaServers.getOrDefault(replicaID, null);
    }

    public ReplicaMetadata addReplicaServer(ReplicaMetadata replica) {
        return replicaServers.put(replica.getId(), replica);
    }

    public int getTotalReplica() {
        if (replicaServers == null) {
            return 0;
        }
        return replicaServers.values().size();
    }

    public void validateReplica() {
        if (replicaServers == null) {
            return;
        } else {
            for (ReplicaMetadata replica : replicaServers.values()) {
                if (replica.getLifeline() <= 0) {
                    replica.executeReset();
                }
            }
        }
    }

    public DatagramSocket getFrontEndSocket() {
        return frontEndSocket;
    }

    public void startUDPListener() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] replyBytes = new byte[1024];
                        DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length);
                        frontEndSocket.receive(replyPacket);
                        String reply = new String(replyPacket.getData(), 0, replyPacket.getLength());
                        String[] params = Commands.generateParamsFromCommand(reply);
                        // TODO handle AKG message
                        System.out.println("Packet:"+reply);
                        String hostAddress = params[1];
                        int port = Integer.valueOf(params[2]);
                        System.out.println(hostAddress + "::" + port);
                        String initResponse = Commands.getAKFReplicaCommand(hostAddress, port);
                        DatagramPacket sendPacket = new DatagramPacket(initResponse.getBytes(), initResponse.length(),
                                Inet4Address.getByName(hostAddress), port);
                        frontEndSocket.send(sendPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        thread.start();
    }
}
