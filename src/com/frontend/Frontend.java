package com.frontend;

import com.frontend.registry.CentralRepository;
import com.frontend.registry.ReplicaMetadata;
import com.frontend.services.impl.AdminImpl;
import com.frontend.services.impl.CustomerImpl;
import com.helper.Commands;
import com.helper.Config;
import com.sun.xml.internal.ws.util.StringUtils;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.*;

/**
 * @author mkjodhani
 * @version 1.0
 * @project Movie Ticket System
 * @since 17/03/23
 */
public class Frontend {
    public static DatagramSocket frontEndSocket;
    public static DatagramSocket heartBeatSocket;
    public static void main(String[] args) throws SocketException, UnknownHostException {
        frontEndSocket = new DatagramSocket(Config.frontendUDPPort);
        heartBeatSocket = new DatagramSocket(Config.frontendHeartBeatSocket);
//        frontEndSocket.setSoTimeout(100);
        // WEB SERVICE running
        CentralRepository centralRepository = CentralRepository.getCentralRepository();

        centralRepository.addReplicaServer(Config.rm1HostAddress,Config.rm1Port);
        centralRepository.addReplicaServer(Inet4Address.getLocalHost().getHostAddress(),Config.rm1Port);

        String localhost = String.format("http://localhost:%d/movie-service", Config.frontendWebPort);
        Endpoint.publish(localhost + "/admin", new AdminImpl());
        Endpoint.publish(localhost + "/customer", new CustomerImpl());
//        centralRepository.startUDPListener();
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] replyBytes = new byte[1024];
                        DatagramPacket requestPacket = new DatagramPacket(replyBytes, replyBytes.length);
                        heartBeatSocket.receive(requestPacket);
                        String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                        System.out.println(request);
                        String[] requestParams = Commands.generateParamsFromCommand(request);
                        String response = "";
                        String replicaID = "";
                        switch (requestParams[0]){
                            case Commands.INT_SERVER:
                                String hostAddress = requestParams[1];
                                int port = Integer.valueOf(requestParams[2]);
                                CentralRepository.getCentralRepository().addReplicaServer(hostAddress,port);
                                response = Commands.getAKFReplicaCommand();
                                break;
                            case Commands.READY_TO_EXECUTE:
                                replicaID = String.format("%s:%s",requestParams[1],requestParams[2]);
                                CentralRepository.getCentralRepository().getReplicaServer(replicaID);
                                // SEND ALL THE COMMANDS TO REPLICA SEPARATED BY NEW_LINE
                                response = Commands.getAKFReplicaCommand();
                                break;
                            case Commands.HEART_BEAT:
                                replicaID = String.format("%s:%s",requestParams[1],requestParams[2]);
                                ReplicaMetadata replica = CentralRepository.getCentralRepository().getReplicaServer(replicaID);
                                replica.setActive(true);
                                response = Commands.getAkgHeartBeatCommand();
                                break;
                        }
                        // TODO handle AKG message
                        DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.length(),
                                requestPacket.getAddress(), requestPacket.getPort());
                        heartBeatSocket.send(sendPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        thread.start();
    }
}
