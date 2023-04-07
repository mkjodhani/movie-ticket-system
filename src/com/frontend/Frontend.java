package com.frontend;

import com.frontend.registry.CentralRepository;
import com.frontend.services.impl.AdminImpl;
import com.frontend.services.impl.CustomerImpl;
import com.shared.Config;

import javax.xml.ws.Endpoint;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author mkjodhani
 * @version 1.0
 * @project Movie Ticket System
 * @since 17/03/23
 */
public class Frontend {
    public static DatagramSocket frontEndSocket;

    public static void main(String[] args) throws SocketException {
        frontEndSocket = new DatagramSocket(Config.frontendUDPPort);
        // WEB SERVICE running
        CentralRepository centralRepository = CentralRepository.getCentralRepository();
        String localhost = String.format("http://localhost:%d/movie-service", Config.frontendWebPort);
        Endpoint.publish(localhost + "/admin", new AdminImpl());
        Endpoint.publish(localhost + "/customer", new CustomerImpl());
//        centralRepository.startUDPListener();
    }
}
