package com.frontend;

import com.frontend.query.FrontEndQuery;
import com.frontend.registry.CentralRepository;
import com.frontend.registry.ReplicaMetadata;
import com.frontend.services.impl.AdminImpl;
import com.frontend.services.impl.CustomerImpl;
import com.helper.Commands;
import com.helper.Config;
import com.sun.xml.internal.ws.util.StringUtils;

import javax.xml.ws.Endpoint;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * @author mkjodhani
 * @version 1.0
 * @project Movie Ticket System
 * @since 17/03/23
 */
public class Frontend {
    private static FileWriter writer;
    private static ArrayList<String> listOfCommand;
    private static String fileName = "file.txt";
    public static DatagramSocket frontEndSocket;
    public static DatagramSocket heartBeatSocket;
    public static void main(String[] args) throws IOException {
//        writer = new FileWriter(fileName, true);
        listOfCommand = new ArrayList<>();
        listOfCommand.add("1,BOOK_MOVIE_TICKET,OUTC1234,ATWM110423,AVENGERS,5");
        frontEndSocket = new DatagramSocket(Config.frontendUDPPort);
        heartBeatSocket = new DatagramSocket(Config.frontendHeartBeatSocket);
        // WEB SERVICE running
        CentralRepository centralRepository = CentralRepository.getCentralRepository();
        for (String replicaHostAddress: Config.replicas){
            centralRepository.addReplicaServer(replicaHostAddress,Config.rm1Port);
        }

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
//                        System.out.println(request);
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
                            case Commands.GET_LIST_OF_REQUESTS:
                                System.out.println("GET_LIST_OF_REQUESTS:;captured");
                                response = Frontend.getListOfCommands();
                                break;
//                            case Commands.READY_TO_EXECUTE:
//                                replicaID = String.format("%s:%s",requestParams[1],requestParams[2]);
//                                CentralRepository.getCentralRepository().getReplicaServer(replicaID).setActive(true);
//                                 SEND ALL THE COMMANDS TO REPLICA SEPARATED BY NEW_LINE
//                                response = Commands.getAKFReplicaCommand();
//                                break;
                            case Commands.HEART_BEAT:
                                replicaID = String.format("%s:%s",requestParams[1],requestParams[2]);
                                ReplicaMetadata replica = CentralRepository.getCentralRepository().getReplicaServer(replicaID);
                                if (replica !=null){
                                    replica.setActive(true);
                                    response = Commands.getAkgHeartBeatCommand();
                                }
                                break;
                        }
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
    public static void addCommand(String command){
        try {
            FileWriter writer = new FileWriter("commands.txt",true);
            listOfCommand.add(command);
            writer.write(command+System.lineSeparator());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getListOfCommands(){
//        return String.join(System.lineSeparator(), listOfCommand);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;

            // read each line of the file and append it to a StringBuilder object
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }

            // close the reader
            reader.close();

            // print the contents of the file
            return stringBuilder.toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static int getTotalCommands(){
        return listOfCommand.size();
    }
}
