/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.replica.theatre;

import com.replica.theatre.movie.Movie;
import com.replica.theatre.movie.Slot;
import com.shared.Admin;
import com.shared.Customer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class TheatreMetaData {

    private String location, prefix;
    private InetAddress inetAddress;
    private int udpPort, webPort;
    private Admin admin;
    private Customer customer;
    private HashMap<String, Movie> movies = new HashMap<>();
    private String endpoint;

    public TheatreMetaData(String ipAddress, String location, String prefix, int port) throws UnknownHostException {
        this.location = location;
        this.prefix = prefix;
        this.udpPort = port + 1100;
        this.webPort = port + 1000;
        this.inetAddress = InetAddress.getByName(ipAddress);
    }

    public boolean isLocalServer(String id) {
        if (id.toLowerCase().contains(this.getPrefix().toLowerCase())) {
            return true;
        }
        return false;
    }

    public String getLocation() {
        return location;
    }

    public String getPrefix() {
        return prefix;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return udpPort;
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

    public HashMap<String, Movie> getMovies() {
        return movies;
    }

    public Slot getSlotByNameAndID(String movieName, String slotID) {
        Movie movie = getMovies().getOrDefault(movieName, null);
        Slot slot = null;
        if (movie != null) {
            slot = movie.getSlots().getOrDefault(slotID, null);
        }
        return slot;
    }

    public int getTotalTicketsFromCustomerID(String customerID, String movieName, String slotID) {
        Slot slot = getSlotByNameAndID(movieName, slotID);
        if (slot == null) {
            return 0;
        } else {
            int totalTicketsForCustomer = slot.getBookedSeats().getOrDefault(customerID, 0);
            return totalTicketsForCustomer;
        }
    }

    public int getAvailableTicketsForSlot(String movieName, String slotID) {
        Slot slot = getSlotByNameAndID(movieName, slotID);
        if (slot == null) {
            return 0;
        } else {
            return slot.getAvailability();
        }
    }

    public int getWebPort() {
        return webPort;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
