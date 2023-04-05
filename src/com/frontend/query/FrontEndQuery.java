package com.frontend.query;

import com.frontend.registry.CentralRepository;
import com.shared.Config;

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

    String query;
    int totalReplica;
    private DatagramSocket frontEndSocket;
    private static final Pattern replicaResponse = Pattern.compile("^(\\d+,)([\\w\\W]+)$");

    private String queryResponse;

    public DatagramSocket getFrontEndSocket() throws SocketException {
        if (frontEndSocket == null) {
            frontEndSocket = new DatagramSocket(Config.FrontendPort);
        }
        return frontEndSocket;
    }

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
            getFrontEndSocket().send(datagramPacket);
            queryResponse = getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResponse() throws IOException {
        String[] responses = new String[CentralRepository.getCentralRepository().getTotalReplica()];
        HashMap<String, Integer> answerCount = new HashMap<>();
        HashMap<Integer, String> answerMap = new HashMap<>();
        int max = 0;
        String maxRepeatedRes = "";
        for (int i = 0; i < responses.length; i++) {
            byte[] array = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
            getFrontEndSocket().receive(datagramPacket);
            String res = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            // TODO get row answer ans compare to other replica answer
            // GET REPLICA ID PUT THE MAPPING IN answerMap
            answerMap.put(getReplicaIdFromResponse(res), getRawDataFromResponse(res));
            // SET THE TOTAL COUNT + 1 in answerCount
            answerCount.put(getRawDataFromResponse(res), answerCount.getOrDefault(getRawDataFromResponse(res), 0) + 1);
            if (max < answerCount.get(getRawDataFromResponse(res))) {
                // COMPARE TO max and update the value
                max = answerCount.get(getRawDataFromResponse(res));
                // ASSIGN the value if necessary maxRepeatedRes
                maxRepeatedRes = getRawDataFromResponse(res);
            } else {
                break;
            }
        }
        return maxRepeatedRes;
    }

    private int getReplicaIdFromResponse(String response) {
        try {
            Matcher matcher = replicaResponse.matcher(response);
            if (matcher.find()) {
                return Integer.valueOf(matcher.group(2));
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
                return matcher.group(4);
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
