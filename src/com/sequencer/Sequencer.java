package com.sequencer;

import com.helper.Commands;
import com.helper.Config;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Sequencer implements Runnable{

    private Thread t;
    private String threadName;
    private int port;
    private boolean wait=false;

    private int seqId = 0;

    Queue<String> InternalQueue = new LinkedList<>();
    HashMap<Integer, Integer> AckCounter = new HashMap<Integer, Integer>();

    public Sequencer(String threadName, int port) {
        this.threadName = threadName;
        this.port = port;
    }

    public void run() {
        DatagramSocket aSocket = null;
        try{
            aSocket = new DatagramSocket(port);
            // create socket at agreed port
            byte[] buffer = new byte[10000];
            while(true){
                System.out.println("Listening");
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                Thread thread = new Thread(() ->{
                    String req = new String(request.getData(),0,request.getLength());
                    System.out.println(req+":Request");
                    for (String replicaHostAddress: Config.replicas){
                        this.Connection(replicaHostAddress,Config.rm1Port, Commands.generateCommandFromParams(new String[]{String.valueOf(seqId) , req}));
                    }
                    AckCounter.put(seqId, 0);
                    seqId++;
                    wait=false;
                });
                thread.start();
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
        System.out.println("Thread " +  threadName + " exiting.");
    }

    public static String Connection(String hostAddress,int serverPort,String s){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
//            InetAddress aHost = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
            byte [] m = s.getBytes();
            DatagramPacket request =
                    new DatagramPacket(m,  s.length(), Inet4Address.getByName(hostAddress), serverPort);
            aSocket.send(request);
            System.out.println("send request to "+hostAddress+":"+serverPort);
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}

        return null;
    }


    public void start () {
        System.out.println("Starting " +  threadName+" on "+port );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    public void stop() {
        t.interrupt();
        System.out.println("Stopping " +  threadName+" on "+port );
    }
}
