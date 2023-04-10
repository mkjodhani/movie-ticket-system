package com.frontend.query;

import com.frontend.Frontend;
import com.frontend.registry.CentralRepository;
import com.helper.Config;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private static final Pattern replicaResponse = Pattern.compile("^(\\d+),((\\w+)::([\\w\\W]+))$");
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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResponse() throws IOException, InterruptedException {
        String[] responses = new String[CentralRepository.getCentralRepository().getTotalReplica()];
        HashMap<String, Integer> answerCount = new HashMap<>();
        Thread[] threads = new Thread[ responses.length];
//        HashMap<String, > answerCount = new HashMap<>();
        final int[] max = {0};
        final String[] maxRepeatedRes = {""};
        for (int i = 0; i < responses.length; i++) {
            threads[i] = new Thread(() ->{
                System.out.println("Thread Started");
                byte[] array = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
                try {
                    Frontend.frontEndSocket.setSoTimeout(1000);
                    Frontend.frontEndSocket.receive(datagramPacket);
                    Frontend.frontEndSocket.setSoTimeout(0);
                    String res = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    System.out.println("res:"+res);
                    answerCount.put(getRawDataFromResponse(res), answerCount.getOrDefault(getRawDataFromResponse(res), 0) + 1);
                    if (max[0] <= answerCount.getOrDefault(getRawDataFromResponse(res),0)) {
                        // COMPARE TO max and update the value
                        max[0] = answerCount.get(getRawDataFromResponse(res));
                        // ASSIGN the value if necessary maxRepeatedRes
                        maxRepeatedRes[0] = getRawDataFromResponse(res);
                    }
                    System.out.println("Thread finished");

                }
                catch (SocketTimeoutException | SocketException e){
                    System.out.println("Skipped due to timeout!!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 0; i < responses.length; i++) {
            executor.submit(threads[i]);
        }
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.SECONDS);
        return maxRepeatedRes[0];
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
