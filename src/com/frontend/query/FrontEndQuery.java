package com.frontend.query;

import com.frontend.Frontend;
import com.frontend.registry.CentralRepository;
import com.helper.Config;
import com.helper.Message;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mkjodhani
 * @project
 * @since 17/03/23
 */
public class FrontEndQuery implements Runnable {
    private String query;
    int totalReplica;
    private static final Pattern replicaResponse = Pattern.compile("^(\\d+),((\\w+):::([\\w\\W]+))$");
    private String queryResponse;
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
            queryResponse = getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResponse() throws IOException {
        String[] responses = new String[CentralRepository.getCentralRepository().getTotalReplica()];
        HashMap<String, Integer> answerCount = new HashMap<>();
//        HashMap<String, > answerCount = new HashMap<>();
        int max = 0;
        String maxRepeatedRes = "";
        System.out.println("-----RES FROM DIFF REPLICA---------");
        for (int i = 0; i < responses.length; i++) {
            byte[] array = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
            try {
                Frontend.frontEndSocket.receive(datagramPacket);
                String res = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                System.out.println(res);
                answerCount.put(getRawDataFromResponse(res), answerCount.getOrDefault(getRawDataFromResponse(res), 0) + 1);
                if (max <= answerCount.getOrDefault(getRawDataFromResponse(res),0)) {
                    // COMPARE TO max and update the value
                    max = answerCount.get(getRawDataFromResponse(res));
                    // ASSIGN the value if necessary maxRepeatedRes
                    maxRepeatedRes = getRawDataFromResponse(res);
                } else {
                    break;
                }
            }
            catch (SocketTimeoutException e){
                System.out.println("Skipped due to timeout!!");
            }
        }
        System.out.println("--------------");
        return maxRepeatedRes;
    }

    private int getReplicaIdFromResponse(String response) {
        try {
            Matcher matcher = replicaResponse.matcher(response);
            if (matcher.find()) {
                return Integer.valueOf(matcher.group(1));
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    private String getRawDataFromResponse(String response) {
        try {
            Matcher matcher = replicaResponse.matcher(response);
            if (matcher.find()) {
                return matcher.group(2);
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
    public String getQueryResponse() {
        return queryResponse;
    }
}
