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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Movie {
    private String name;
    private HashMap<String, Slot> slots;
    private TheatreMetaData metaData;

    private Movie(String name, TheatreMetaData metaData) {
        this.name = name;
        this.metaData = metaData;
        slots = new HashMap<>();
    }

    public static Movie addMovie(String name, TheatreMetaData metaData) {
        if (metaData.getMovies().get(name) == null) {
            Movie movie = new Movie(name, metaData);
            metaData.getMovies().put(name, movie);
            // Theatre.LOGGER.log(Level.INFO,String.format("Movie: %s added to the theatre
            // at %s.",name,metaData.getLocation()));
            return movie;
        }
        return null;
    }

    public Slot addSlot(String slotId, int numberOfSeats) throws Exception {
        Slot slot = slots.get(slotId);
        if (slot == null) {
            slot = new Slot(slotId, numberOfSeats, this.name, this.metaData);
            slots.put(slotId, slot);
            Theatre.LOGGER.log(Level.INFO, String.format("New slot for %s added for %s.", slotId, this.name));
        } else {
            Theatre.LOGGER.log(Level.INFO, String.format("Total number of seats updated from %d to %d for %s.",
                    slot.getTotalSeats(), numberOfSeats, slotId));
            slot.setTotalSeats(numberOfSeats);
        }
        return slot;
    }

    public Slot removeSlot(String slotId) {
        Slot deletedSlot = slots.remove(slotId);
        if (deletedSlot == null) {
            Theatre.LOGGER.log(Level.INFO, String.format("No slot found for %s.", slotId));
        } else {
            Theatre.LOGGER.log(Level.INFO, String.format("Slot deleted for %s(%s).", this.name, slotId));
        }
        return deletedSlot;
    }

    public String[] getSeats() {
        String[] seats = new String[slots.size()];
        int index = 0;
        for (Slot slot : slots.values()) {
            seats[index++] = slot.getSlotId() + " " + slot.getAvailability();
        }
        Theatre.LOGGER.log(Level.INFO, String.format("Fetching seat availability for %s", this.name));
        return seats;
    }

    public HashMap<String, Slot> getSlots() {
        return slots;
    }

    public String getCustomerTickets(String customerId) {
        Theatre.LOGGER.log(Level.INFO, String.format("Fetching customer schedule for %s.", this.name));
        ArrayList<String> tickets = new ArrayList<>();
        for (Slot slot : getSlots().values()) {
            String ticket = slot.getTicketsByCustomerId(customerId);
            if (ticket != null && !ticket.equals("")) {
                String t = String.format("%s %s", this.name, ticket);
                tickets.add(t);
            }
        }
        if (tickets.size() == 0) {
            return "";
        }
        String[] array = new String[tickets.size()];
        int index = 0;
        for (String t1 : tickets) {
            array[index++] = t1;
        }
        return Commands.generateCommandFromParams(array);
    }

}
