package com.frontend.query;

import com.frontend.Frontend;
import com.frontend.registry.CentralRepository;
import com.frontend.registry.ReplicaMetadata;
import com.helper.Commands;
import com.helper.Config;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mkjodhani
 * @project
 * @since 17/03/23
 */
public class FrontEndQuery implements Runnable {
    private static int delayTime = 1000;
    private String query;
    int totalReplica;
    private int maxCommonCount = 0;
    public static final Pattern replicaResponse = Pattern.compile("^(\\d+),((\\w+)::([\\w\\W]+))$");
    public String queryResponse;
    private HashMap<String,String> responseReplicaHash;
    private HashMap<String,Integer> votes;
    public FrontEndQuery(String query) throws SocketException {
        CentralRepository centralRepository = CentralRepository.getCentralRepository();
        this.totalReplica = centralRepository.getTotalReplica();
        this.query = query;
    }

    @Override
    public void run() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(query.getBytes(), query.length(),
                    Inet4Address.getLocalHost(), Config.sequencerPort);
            Frontend.frontEndSocket.send(datagramPacket);
            getResponse();
            updateLifelines();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRestartCommand(String hostAddress) {
        String restartCommand = Commands.getRestartReplicaCommand(hostAddress,Config.rm1Port);
        sendStringToReplicaManager(hostAddress,restartCommand);
    }
    public static String sendStringToReplicaManager(String hostAddress,String string){
        try {
            CentralRepository.getCentralRepository().getReplicaServer(String.format("%s:%d",hostAddress,Config.rm1Port)).setActive(false);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(string.getBytes(),string.length(),Inet4Address.getByName(hostAddress),Config.rm1Port);
            socket.send(datagramPacket);
            System.out.println("sendStringToReplicaManager::"+"sent");
            byte[] array = new byte[1024];
            DatagramPacket recievedPacket = new DatagramPacket(array, array.length);
            socket.receive(recievedPacket);
            String res = new String(recievedPacket.getData(), 0, recievedPacket.getLength());
            System.out.println("sendStringToReplicaManager::res:"+res);
            if (res.equals(Commands.READY_TO_EXECUTE)){
                CentralRepository.getCentralRepository().getReplicaServer(String.format("%s:%d",hostAddress,Config.rm1Port)).setActive(true);
            }
            return res;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateLifelines() throws SocketException, InterruptedException {
        for (String answer: votes.keySet()){
            if (votes.get(answer) < maxCommonCount){
                for (String hostAddress: responseReplicaHash.keySet()){
                    if (responseReplicaHash.get(hostAddress).equals(answer)){
                        if (CentralRepository.getCentralRepository().getReplicaServer(hostAddress).getLifeline() == 0){
                            CentralRepository.getCentralRepository().getReplicaServer(hostAddress).setActive(false);
                            sendRestartCommand(hostAddress);
                        }
                        else {
                            CentralRepository.getCentralRepository().getReplicaServer(hostAddress).reduceLifeLine();
                        }
                    }
                }
            }
        }
    }
    private void getResponse() throws IOException, InterruptedException {
        String[] responses = new String[CentralRepository.getCentralRepository().getTotalActiveReplica()];
        responseReplicaHash = new HashMap<>();
        ArrayList<String> replicaList = new ArrayList<>(Arrays.asList(Config.replicas));
        HashMap<String, Integer> answerCount = new HashMap<>();
        ArrayList<String> arrayList = new ArrayList<>();
//        Thread[] threads = new Thread[ responses.length];
//        final int[] max = {0};
//        final String[] maxRepeatedRes = {""};
        System.out.println("-----------RESPONSE FROM REPLICA-----------");
        for (int i = 0; i < responses.length; i++) {
//            threads[i] = new Thread(() ->{
                byte[] array = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
                try {
                    long startTime = System.currentTimeMillis();
                    Frontend.frontEndSocket.setSoTimeout(delayTime * 100);
                    Frontend.frontEndSocket.receive(datagramPacket);
                    Frontend.frontEndSocket.setSoTimeout(0);
                    replicaList.remove(datagramPacket.getAddress().getHostAddress());


                    long endTime = System.currentTimeMillis();
                    long timeTaken = endTime - startTime;
                    if (delayTime == 1000){
                        delayTime = (int)timeTaken;
                    }else {
                        delayTime = (int)(delayTime * Frontend.getTotalCommands() + timeTaken)/(Frontend.getTotalCommands() + 1);
                    }
                    String res = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).toLowerCase();
                    if (queryResponse == null){
                        queryResponse = res;
                    }
                    System.out.println(datagramPacket.getAddress().getHostAddress()+":::"+res);
//                    responseReplicaHash.put(datagramPacket.getAddress().getHostAddress(),res);
                    arrayList.add(res);
                    answerCount.put(res,answerCount.getOrDefault(res,0)+1);
                }
                catch (SocketTimeoutException | SocketException e){
                    System.out.println("Skipped due to timeout!!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//            });
        }
        System.out.println("----------------------");

        for (String restartReplica:replicaList){
            Thread thread = new Thread(() ->{
                sendRestartCommand(restartReplica);
            });
            thread.start();
        }
        votes = compareAnswer(arrayList);
        System.out.println("AFTER COMAPRASION::"+queryResponse);
        if (queryResponse == null){
            queryResponse = Commands.getErrorCommand("Something went wrong!");
        }
    }
    private String[]  getSetOfItems(String string){
        String itemString = string.toLowerCase();
        ArrayList<String> arrayList = new ArrayList();
        for (String item:itemString.split(Commands.DELIMITER)){
            if (!item.equals("") && !item.equals("success::") && !item.equals("error::")){
                arrayList.add(item.trim());
            }
        }
        String[] arr = new String[arrayList.size()];
        for (int i=0;i<arr.length;i++){
            arr[i] = arrayList.get(i).trim();
        }
        Arrays.sort(arr);
        return arr;
    }
    public static boolean compareArrays(String[] arr1, String[] arr2) {
        // Sort both arrays
        Arrays.sort(arr1);
        Arrays.sort(arr2);

        // Compare the sorted arrays element by element
        for (int i = 0; i < arr1.length; i++) {
            if (!arr1[i].equals(arr2[i])) {
                return false;
            }
        }

        return true;
    }
    private HashMap<String,Integer> compareAnswer(ArrayList<String> answers) {
        HashMap<String, Integer> votes = new HashMap<>();
        if (query.toLowerCase().contains(Commands.LIST_MOVIE_AVAILABILITY.toLowerCase()) || query.toLowerCase().contains(Commands.GET_CUSTOMER_SCHEDULE.toLowerCase())) {
            // TODO do the iterative approach for these command
            for (int i = 0; i < answers.size(); i++) {
                if (queryResponse == null) {
                    queryResponse = answers.get(i);
                }
                votes.put(answers.get(i), 0);
                String[] set1 = getSetOfItems(getRawDataFromResponse(answers.get(i)));
                for (int j = 0; j < answers.size(); j++) {
                    if (i != j) {
                        String[] set2 = getSetOfItems(getRawDataFromResponse(answers.get(j)));
                        if (compareArrays(set2, set1)) {
                            System.out.println(answers.get(i));
                            System.out.println("AND");
                            System.out.println(answers.get(j));
                            votes.put(answers.get(i), votes.get(answers.get(i)) + 1);
                        }
                    }
                }
                if (votes.get(answers.get(i)) >= maxCommonCount) {
                    maxCommonCount = votes.getOrDefault(answers.get(i), 0);
                    queryResponse = answers.get(i);
                    System.out.println("UPDATE FINAL ANSWER:"+queryResponse);
                }
            }
        }
        else {
            for (String ans:answers){
                Matcher matcher1 = replicaResponse.matcher(ans);
                for (String ans_:answers) {
                    Matcher matcher2 = replicaResponse.matcher(ans_);
                    if (matcher2.find() && matcher1.find()){
                        if ( matcher1.group(3).equals(matcher2.group(3))){
                            votes.put(ans,votes.getOrDefault(ans,0)+1);
                            if (votes.getOrDefault(ans,0) > maxCommonCount){
                                maxCommonCount = votes.getOrDefault(ans,0);
                                queryResponse = ans;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("----------COMAPARING----------");
        for (String key:votes.keySet()){
            System.out.println(key+"::::===>>"+votes.get(key));
        }
        System.out.println("----------COMAPARING----------");
        return votes;
    }
    private String getRawDataFromResponse(String response) {
        if (query.toLowerCase().contains(Commands.LIST_MOVIE_AVAILABILITY.toLowerCase()) || query.toLowerCase().contains(Commands.GET_CUSTOMER_SCHEDULE.toLowerCase()) ){
            Pattern replicaResponse = Pattern.compile("^(\\d+),([\\w\\W]+)$");
            try {
                Matcher matcher = replicaResponse.matcher(response);
                if (matcher.find()) {
                    return matcher.group(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }else {
            try {
                Matcher matcher = replicaResponse.matcher(response);
                if (matcher.find()) {
                    return matcher.group(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }
    public String getQueryResponse() {
        System.out.println("getRawDataFromResponse(queryResponse)::"+getRawDataFromResponse(queryResponse));
        return getRawDataFromResponse(queryResponse);
    }
}
