package com.sequencer;

import com.shared.Config;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;

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
        System.out.println("Running " +  threadName );
        DatagramSocket aSocket = null;
        int counter =0;
        try{
            aSocket = new DatagramSocket(port);
            // create socket at agreed port
            byte[] buffer = new byte[10000];
            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String req = new String(request.getData(),0,request.getLength());

                System.out.println("Thread " +  threadName +": " +req);
                // Add to queue
//                if(req.charAt(0)=='Q'){
//                    InternalQueue.add(req);
//                }
                if(req.charAt(0)=='A'){
                    System.out.println("Thread " +  threadName +": " + "ACK");
                    continue;
                }
//                if(req.charAt(0)=='A' && req.charAt(1)==seqId-1 && this.wait){
//                    AckCounter.put(seqId-1,AckCounter.get(seqId-1)+1);
//                    if(AckCounter.get(seqId-1)>=1){
//                        this.wait=false;
//                    }
//                    continue;
//                }
//
//                if(!this.wait) {

//                sockets.add(this.Connection(Config.rm1Port,seqId + req));
//                sockets.add(this.Connection(Config.rm2Port,seqId + req));
//                sockets.add(this.Connection(Config.rm3Port,seqId + req));
//                sockets.add(this.Connection(Config.rm4Port,seqId + req));
                System.out.println(req+":Before N Unicast");
                    String res = this.Connection(Config.rm1Port, seqId + req);
//                    this.Connection(Config.rm2Port, seqId + req);
//                    this.Connection(Config.rm3Port, seqId + req);
//                    this.Connection(Config.rm4Port, seqId + req);
                System.out.println(res+":After N Unicast");

                    AckCounter.put(seqId, 0);
                    seqId++;


                    wait=false;
//                }

//                    String res = req;
                    System.out.println("Thread " + threadName + ": " + "ACK");

//                System.out.println(res);
//                    byte[] m = res.getBytes();
//
//                    DatagramPacket reply = new DatagramPacket(m, res.length(),
//                            request.getAddress(), request.getPort());
//                    aSocket.send(reply);
//                }
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
            System.out.println("recieved");
            String response = new String (reply.getData(),0, reply.getLength());
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
