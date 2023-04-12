/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.replica.theatre.query;

import com.helper.Commands;
import com.helper.Input;
import com.helper.Message;
import com.helper.Role;
import com.replica.manager.ReplicaManager;
import com.replica.theatre.TheatreMetaData;
import com.replica.theatre.movie.Movie;
import com.replica.theatre.movie.Slot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Actions {
    private TheatreMetaData metaData;
    static private Logger LOGGER = Logger.getLogger(Movie.class.getName());

    public Actions(TheatreMetaData metaData) {
        this.metaData = metaData;
    }

    public String addMovieSlot(String command) {
        Message response;
        try {
            String[] args = command.split(Commands.DELIMITER);
            String movieName = args[2], movieID = args[1];
            int bookingCapacity = Integer.parseInt(args[3]);
            response = Message.generateMessageFromString(addMovieSlotByLocal(movieID, movieName, bookingCapacity));
            LOGGER.log(Level.SEVERE, String.format("Add movie slot using UDP command:%s", command));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    public String removeMovieSlot(String command) {
        Message response;
        try {
            String[] args = command.split(Commands.DELIMITER);
            String movieName = args[2], movieID = args[1];
            response = Message.generateMessageFromString(removeMovieSlotByLocal(movieID, movieName));
            LOGGER.log(Level.INFO, String.format("Remove movie slot using UDP command : %s", command));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    public String checkAvailability(String command) {
        Message response;
        try {
            String result = "";
            String[] args = command.split(Commands.DELIMITER);
            // CMD MOVIE_NAME
            String movieName = args[1];
            Movie movie = metaData.getMovies().get(movieName);
            if (movie == null) {
                // return Message.getErrorMessage(("No movie found by name of " + movieName));
            } else {
                result += Commands.generateCommandFromParams(movie.getSeats());
            }
            response = Message.getSuccessMessage(result);
            LOGGER.log(Level.SEVERE, String.format("Check availability using UDP command:%s", command));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    public String getBookingSchedule(String command) {
        String[] args = Commands.generateParamsFromCommand(command);
        String customerID = args[1];
        ArrayList<String> response = new ArrayList<>();
        Message message;
        for (Movie movie : metaData.getMovies().values()) {
            String ticket = movie.getCustomerTickets(customerID);
            if (!ticket.equals("")) {
                response.add(ticket);
            }
        }
        LOGGER.log(Level.INFO, String.format("Get booking schedule using UDP command:%s", command));
        message = Message.getSuccessMessage(String.join(Commands.DELIMITER, response));
        return message.getMessage();
    }

    public final String addMovieTickets(String command) {
        Message response;
        String[] args = command.split(Commands.DELIMITER);
        String customerID = args[1], movieID = args[2], movieName = args[3];
        int numberOfTickets = Integer.parseInt(args[4]);
        LOGGER.log(Level.INFO, String.format("Add movie tickets using UDP command:%s", command));
        ArrayList<String> tickets = new ArrayList<>();
        for (String serverPrefix : ReplicaManager.locationPorts.keySet()) {
            if (serverPrefix.toLowerCase().equals(Role.getLocationPrefix(customerID))) {
                continue;
            } else if (serverPrefix.toLowerCase().equals(Role.getLocationPrefix(movieID))) {
                Actions actions = new Actions(metaData);
                Message localMessage = Message.generateMessageFromString(
                        actions.getBookingSchedule(Commands.getBookingScheduleCommand(customerID)));
                String localResponse = localMessage.extractMessage();
                for (String movieTicket : localResponse.split(Commands.DELIMITER)) {
                    tickets.add(movieTicket);
                }
            } else {
                // SEND COMMAND TO REMOTE SEVER BASED ON PREFIX AND GET RESPONSE
                try {
                    Message remoteMessage = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                            Commands.getBookingScheduleCommand(customerID), serverPrefix));
                    String remoteResponse = remoteMessage.extractMessage();
                    for (String movieTicket : remoteResponse.split(Commands.DELIMITER)) {
                        tickets.add(movieTicket);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (tickets.size() > 3) {
            response = Message.getErrorMessage(
                    "You have already booked tickets three times for the next week.Please try to book a ticket later.");
        } else {
            response = Message
                    .generateMessageFromString(addMovieTicketsByLocal(customerID, movieID, movieName, numberOfTickets));
        }
        return response.getMessage();
    }

    public String cancelMovieTickets(String command) {
        String[] args = command.split(Commands.DELIMITER);
        String customerID = args[1], movieID = args[2], movieName = args[3];
        int numberOfTickets = Integer.parseInt(args[4]);
        LOGGER.log(Level.SEVERE, String.format("Add movie tickets using UDP command:%s", command));
        return cancelMovieTicketsByLocal(customerID, movieID, movieName, numberOfTickets);
    }

    public String getAvailableSeatsByMovie(String command) {
        String[] args = command.split(Commands.DELIMITER);
        String movieID = args[1], movieName = args[2];
        LOGGER.log(Level.SEVERE, String.format("Add movie tickets using UDP command:%s", command));
        int totalTickets = metaData.getAvailableTicketsForSlot(movieName, movieID);
        Message response = Message.getSuccessMessage(String.valueOf(totalTickets));
        return response.getMessage();
    }

    public String getCustomerSeatsByMovieSlot(String command) {
        String[] args = command.split(Commands.DELIMITER);
        String customerID = args[1], movieID = args[2], movieName = args[3];
        LOGGER.log(Level.SEVERE, String.format("Add movie tickets using UDP command:%s", command));
        int totalTickets = metaData.getTotalTicketsFromCustomerID(customerID, movieName, movieID);
        Message response = Message.getSuccessMessage(String.format("%d", totalTickets));
        return response.getMessage();
    }

    public String addMovieTicketsByLocal(String customerID, String movieID, String movieName, int numberOfTickets) {
        Message response;
        Movie movie = metaData.getMovies().get(movieName);
        if (movie == null) {
            response = Message
                    .getErrorMessage("Movie is not listed for " + movieName + " at " + metaData.getLocation() + ".");
        } else {
            Slot selectedSlot = movie.getSlots().get(movieID);
            if (selectedSlot == null) {
                response = Message.getErrorMessage("Movie slot is not listed for " + movieID + "(" + movieName + ") at "
                        + metaData.getLocation() + ".");
            } else {
                if (selectedSlot.addTicket(customerID, numberOfTickets)) {
                    response = Message.getSuccessMessage(numberOfTickets + " tickets are booked for " + movieName
                            + " at " + metaData.getLocation() + ".");
                } else {
                    response = Message.getErrorMessage(String.format("%d tickets are not available for %s(%s) at %s.",
                            numberOfTickets, movieName, selectedSlot.getSlotId(), metaData.getLocation()));
                }
            }
        }
        return response.getMessage();
    }

    public String getAvailableSeatsByMovieByLocal(String movieID, String movieName) {
        Message response;
        Movie movie = metaData.getMovies().get(movieName);
        if (movie == null) {
            response = Message
                    .getErrorMessage("Movie is not listed for " + movieName + " at " + metaData.getLocation() + ".");
        } else {
            Slot selectedSlot = movie.getSlots().get(movieID);
            if (selectedSlot == null) {
                response = Message.getErrorMessage(
                        "Movie is not listed for " + movieName + " at " + metaData.getLocation() + ".");
            } else {
                response = Message.getSuccessMessage(
                        String.format("%s %s %d", movieID, movieName, selectedSlot.getAvailability()));
            }
        }
        return response.getMessage();
    }

    public String cancelMovieTicketsByLocal(String customerID, String movieID, String movieName, int numberOfTickets) {
        Message response;
        Movie movie = metaData.getMovies().get(movieName);
        if (movie == null) {
            response = Message
                    .getSuccessMessage("Movie is not listed for " + movieName + " at " + metaData.getLocation() + ".");
        } else {
            Slot selectedSlot = movie.getSlots().get(movieID);
            if (selectedSlot == null) {
                response = Message.getSuccessMessage(
                        "Movie is not listed for " + movieName + " at " + metaData.getLocation() + ".");
            } else {
                if (selectedSlot.cancelTickets(customerID, numberOfTickets)) {
                    response = Message.getSuccessMessage(numberOfTickets + " tickets are cancelled  for " + movieName
                            + " at " + metaData.getLocation() + ".");
                } else {
                    response = Message.getErrorMessage(numberOfTickets + " tickets are not booked for " + movieName
                            + " at " + metaData.getLocation() + ".");
                }
            }
        }
        return response.getMessage();
    }

    public String addMovieSlotByLocal(String movieID, String movieName, int bookingCapacity) throws Exception {
        Message message;
        Movie movie = metaData.getMovies().getOrDefault(movieName, null);
        if (movie == null) {
            message = Message.getErrorMessage(String.format("There is no movie named %s.", movieName));
        } else {
            boolean alreadyExist = movie.getSlots().getOrDefault(movieID, null) != null;
            movie.addSlot(movieID, bookingCapacity);
            if (alreadyExist) {
                message = Message.getSuccessMessage(
                        String.format("Movie slot for %s for %s is successfully updated with %d capacity.", movieName,
                                movieID, bookingCapacity));
            } else {
                message = Message.getSuccessMessage(
                        String.format("Movie slot for %s for %s is successfully created with %d capacity.", movieName,
                                movieID, bookingCapacity));
            }
        }
        return message.getMessage();
    }

    public String removeMovieSlotByLocal(String movieID, String movieName) {
        Message response;
        Movie movie = metaData.getMovies().get(movieName);
        if (movie == null) {
            response = Message.getErrorMessage("No movie found by name of " + movieName);
        } else {
            Slot deletedSlot = movie.removeSlot(movieID);
            if (deletedSlot == null) {
                response = Message.getErrorMessage("No slot found by ID of " + movieID);
            } else {
                String nextSlotID = Input.getNextAvailableSlotID(movie.getSlots().keySet(), movieID);
                if (deletedSlot.getBookedTickets() == 0){
                    response = Message.getSuccessMessage(String.format(
                            "Movie slot for %s for %s is successfully deleted.Also the next slot(%s) is extended to %d tickets.",
                            movieName, movieID, nextSlotID, deletedSlot.getBookedTickets()));
                }
                else if (nextSlotID == null ) {
                    movie.getSlots().put(deletedSlot.getSlotId(), deletedSlot);
                    response = Message.getErrorMessage("No slots available to transfer the booked seats of " + movieID
                            + " for " + movieName + ".");
                    LOGGER.log(Level.INFO,
                            "Reverting the deletion operation for slot(" + deletedSlot.getSlotId() + ").");
                } else {
                    Slot nextSlot = movie.getSlots().get(nextSlotID);
                    nextSlot.transferSeats(deletedSlot.getBookedSeats());
                    response = Message.getSuccessMessage(String.format(
                            "Movie slot for %s for %s is successfully deleted.Also the next slot(%s) is extended to %d tickets.",
                            movieName, movieID, nextSlotID, deletedSlot.getBookedTickets()));
                }
            }
        }
        LOGGER.log(Level.INFO, response.getMessage());
        return response.getMessage();
    }
}
