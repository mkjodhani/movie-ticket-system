package com.helper;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Config {
    public static int rm1Port = 3001; // UDP FOR RM
    public static int sequencerPort = 3005; // UDP SEQUENCER
    public static int frontendUDPPort = 3006; // UDP SERVER
    public static int frontendHeartBeatSocket = 9999; // UDP SERVER
    public static int frontendWebPort = 3000; // WEB SERVICE
    public static String[] replicas = new String[]{
//            "192.168.241.160",
            "192.168.0.119",
//            "192.168.241.142",
//            "192.168.241.225",
    };
    public static String localAddress;
    public static String frontEndAddress;
    static {
        try {
            localAddress = Inet4Address.getLocalHost().getHostAddress();
            frontEndAddress = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}