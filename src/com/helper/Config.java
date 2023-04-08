package com.helper;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Config {
    public static int rm1Port = 3001; // UDP FOR RM
    public static int sequencerPort = 3005; // UDP SEQUENCER
    public static int frontendUDPPort = 3006; // UDP SERVER
    public static int frontendHeartBeatSocket = 9999; // UDP SERVER
    public static int frontendWebPort = 3000; // WEB SERVICE
    public static String rm1HostAddress = "192.168.0.157";
    public static String localAddress;
    public static String frontEndAddress;
    static {
        try {
            localAddress = Inet4Address.getLocalHost().getHostAddress();
            frontEndAddress =Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}