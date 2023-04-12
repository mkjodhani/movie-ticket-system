/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.replica.theatre.movie;

import com.helper.Commands;
import com.replica.theatre.Theatre;
import com.replica.theatre.TheatreMetaData;
import com.shared.Customer;

import java.util.HashMap;
import java.util.logging.Level;

public class Slot {
    private String slotId, movieName;
    private int totalSeats, bookedTickets;
    private TheatreMetaData metaData;
    private HashMap<String, Integer> bookedSeats;

    public Slot(String slotId, int totalSeats, String movieName, TheatreMetaData metaData) {
        this.movieName = movieName;
        this.slotId = slotId;
        this.totalSeats = totalSeats;
        this.bookedSeats = new HashMap<>();
        this.metaData = metaData;
    }

    public String getSlotId() {
        return slotId;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) throws Exception {
        if (this.bookedTickets <= totalSeats) {
            this.totalSeats = totalSeats;
        } else {
            throw new Exception("Already booked seats are more than the updated seat count.");
        }
    }

    public int getAvailability() {
        return totalSeats - bookedTickets;
    }

    public int getBookedTickets() {
        return bookedTickets;
    }

    public HashMap<String, Integer> getBookedSeats() {
        return bookedSeats;
    }

    synchronized public boolean addTicket(String customerId, int totalNumberOfSeats) {
        // TODO check the customer is eligible for booking at given slot
        if (!isCustomerEligibleForTicket(customerId)) {
            Theatre.LOGGER.log(Level.INFO,
                    "Customer have already booked for the same time slot for different location.");
            return false;
        }
        if (this.bookedTickets + totalNumberOfSeats > this.totalSeats) {
            Theatre.LOGGER.log(Level.INFO, String.format("%d tickets not available to be booked for the moment for %s.",
                    totalNumberOfSeats, this.slotId));
            return false;
        }
        this.bookedSeats.put(customerId, this.bookedSeats.getOrDefault(customerId,0) + totalNumberOfSeats);
        this.bookedTickets += totalNumberOfSeats;
        Theatre.LOGGER.log(Level.INFO,
                String.format("Booked %d tickets for %s by %s.", totalNumberOfSeats, this.slotId, customerId));
        return true;
    }

    private boolean isCustomerEligibleForTicket(String customerId) {
        Customer customerService = metaData.getCustomer();
        String schedule = customerService.getBookingSchedule(customerId);
        if (schedule.equals("")) {
            return true;
        }
        for (String ticket : schedule.split(Commands.DELIMITER)) {
            if (ticket.contains(slotId.substring(3))
                    && !(ticket.contains(this.slotId) && ticket.contains(this.movieName))) {
                return false;
            }
        }
        return true;
    }

    synchronized public boolean cancelTickets(String customerId, int totalNumberOfSeats) {
        int currentBookedTickets;
        try {
            currentBookedTickets = this.bookedSeats.get(customerId);
        } catch (Exception e) {
            Theatre.LOGGER.log(Level.INFO,
                    String.format("No tickets booked for the customer %s for %s.", customerId, this.slotId));
            return false;
        }
        if (currentBookedTickets >= totalNumberOfSeats) {
            this.bookedSeats.put(customerId, currentBookedTickets - totalNumberOfSeats);
            this.bookedTickets -= totalNumberOfSeats;
            Theatre.LOGGER.log(Level.INFO, String.format("%d tickets cancelled in account of %s for %s.",
                    totalNumberOfSeats, customerId, this.slotId));
            return true;
        } else {
            Theatre.LOGGER.log(Level.INFO, String.format("%d tickets not available to be booked for the moment for %s.",
                    totalNumberOfSeats, this.slotId));
            return false;
        }
    }

    public void transferSeats(HashMap<String, Integer> seats) {
        Theatre.LOGGER.log(Level.INFO,
                String.format("transferring tickets for %d customers to slot(%s).", seats.size(), this.slotId));
        this.totalSeats += seats.size();
        for (String customerId : seats.keySet()) {
            addTicket(customerId, seats.get(customerId));
        }
    }

    public String getTicketsByCustomerId(String customerId) {
        try {
            int totalBookedSeats = bookedSeats.get(customerId);
            return String.format("%s %s", this.slotId, totalBookedSeats);
        } catch (Exception e) {
            return null;
        }
    }
}
