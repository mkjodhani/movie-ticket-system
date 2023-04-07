package com.sequencer;

import com.helper.Commands;
import com.shared.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
                String req = new String(request.getData(),0,request.getLength());
                System.out.println(req+":Request");
                String res = this.Connection(Config.rm1Port, Commands.generateCommandFromParams(new String[]{String.valueOf(seqId) , req}));
                System.out.println(res+":Reply");
                AckCounter.put(seqId, 0);
                seqId++;
                wait=false;
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
        System.out.println("Thread " +  threadName + " exiting.");
    }

    public static String Connection(int serverPort,String s){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
            byte [] m = s.getBytes();
            DatagramPacket request =
                    new DatagramPacket(m,  s.length(), aHost, serverPort);
            aSocket.send(request);
            System.out.println("sent");
            byte[] buffer = new byte[10000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            String response = new String (reply.getData(),0, reply.getLength());
            System.out.println("recieved:"+response);
            System.out.println(response);
            return response;
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
