/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.replica.theatre;

import com.replica.manager.ReplicaManager;
import com.replica.theatre.movie.Movie;
import com.replica.theatre.query.Query;
import com.replica.theatre.web.Publisher;
import com.shared.Admin;
import com.shared.Customer;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Theatre implements Runnable {
    static public Logger LOGGER;
    private TheatreMetaData metaData;
    private DatagramSocket datagramSocket;
    private Admin admin;
    private Customer customer;

    public Theatre(int port, String location, String prefix){
        metaData = ReplicaManager.locationPorts.get(prefix);
        LOGGER = com.helper.Logger.getLogger(location, true);
    }

    @Override
    public void run() {
        try {
            Publisher publisher = new Publisher(metaData);
            publisher.publish();
            datagramSocket = new DatagramSocket(metaData.getPort());
            setInitialState();
            LOGGER.log(Level.INFO, String.format("UDP server started to listen request for %s at %d port",
                    this.metaData.getLocation(), this.metaData.getPort()));
            System.out.println(metaData.getLocation() + " server is started at " + metaData.getPort() + "(UDP) and "
                    + metaData.getEndpoint() + ".");
            // start UDP listener
            while (true) {
                try {
                    byte[] bytes = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(bytes, bytes.length);
                    this.datagramSocket.receive(receivedPacket);
                    Query query = new Query(receivedPacket, metaData);
                    Thread thread = new Thread(query);
                    thread.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setInitialState() {
        String[] movieList = new String[] { "Avengers", "Avatar", "Titanic" };
        for (String movieName : movieList) {
            Movie.addMovie(movieName.toLowerCase(), this.metaData);
        }
    }
    public Admin getAdmin() {
        return admin;
    }
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
