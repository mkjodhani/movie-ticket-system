package com.frontend.registry;

import com.helper.Commands;

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
    private HashMap<String, ReplicaMetadata> replicaServers;
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

    public ReplicaMetadata getReplicaServer(String replicaID) {
        return replicaServers.getOrDefault(replicaID, null);
    }

    public ReplicaMetadata addReplicaServer(String hostAddress,int port) {
        ReplicaMetadata replica = new ReplicaMetadata(hostAddress,port,false);
        return replicaServers.put(replica.getId(), replica);
    }

    public int getTotalReplica() {
        if (replicaServers == null) {
            return 0;
        }
        return replicaServers.values().size();
    }
    public int getTotalActiveReplica() {
        int total = 0;
        for (ReplicaMetadata replicaMetadata: replicaServers.values()){
            if (replicaMetadata.isActive()){
                total++;
            }
        }
        return total;
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
}
