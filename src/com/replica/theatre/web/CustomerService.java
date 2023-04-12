package com.replica.theatre.web;

import com.helper.Commands;
import com.helper.Message;
import com.helper.Role;
import com.replica.manager.ReplicaManager;
import com.replica.theatre.TheatreMetaData;
import com.replica.theatre.query.Actions;
import com.replica.theatre.query.Query;
import com.shared.Customer;
import javax.jws.WebService;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author mkjodhani
 * @project
 * @since 11/03/23
 */

@WebService(endpointInterface = "com.shared.Customer")
public class CustomerService implements Customer {
    TheatreMetaData metaData;

    public CustomerService(TheatreMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
            String newMovieName, int numberOfTickets) {
        Message response = null;
        // whether the cx has booked tickets for oldMovieName and movieID or not
        boolean isTicketExists = false;
        // whether the tickets are available to book for newMovieName and newMovieID or
        // not
        boolean isTicketsAvailable = false;
        // calculate isTicketExists value
        if (metaData.isLocalServer(movieID)) {
            isTicketExists = metaData.getTotalTicketsFromCustomerID(customerID, oldMovieName,
                    movieID) >= numberOfTickets;
        } else {
            try {
                Message remoteResponse = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.getCustomerSeatsByMovieSlotCommand(customerID, movieID, oldMovieName),
                        Role.getLocationPrefix(movieID)));
                isTicketExists = Integer.parseInt(remoteResponse.extractMessage()) >= numberOfTickets;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // calculate isTicketExists value
        if (metaData.isLocalServer(newMovieID)) {
            isTicketsAvailable = metaData.getAvailableTicketsForSlot(newMovieName, newMovieID) >= numberOfTickets;
        } else {
            try {
                Message remoteResponse = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.getAvailableSeatsByMovieCommand(newMovieID, newMovieName),
                        Role.getLocationPrefix(newMovieID)));
                isTicketsAvailable = Integer.parseInt(remoteResponse.extractMessage()) >= numberOfTickets;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (!isTicketExists) {
                response = Message.getErrorMessage(numberOfTickets
                        + String.format(" tickets are not booked at %s(%s) so it can not be exchanged to %s(%s).",
                                movieID, oldMovieName, newMovieID, newMovieName));
            } else if (!isTicketsAvailable) {
                response = Message.getErrorMessage(numberOfTickets
                        + String.format(" tickets are not available for booking at %s(%s).", newMovieID, newMovieName));
            } else {
                Message remoteResponse;
                remoteResponse = Message.generateMessageFromString(
                        metaData.getCustomer().bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets));
                if (remoteResponse.getType().equals(Message.SUCCESS)) {
                    remoteResponse = Message.generateMessageFromString(metaData.getCustomer()
                            .cancelMovieTickets(customerID, movieID, oldMovieName, numberOfTickets));
                    if (remoteResponse.getType().equals(Message.SUCCESS)) {
                        response = Message.getErrorMessage(
                                numberOfTickets + String.format(" tickets are exchanged from %s(%s) to %s(%s).",
                                        movieID, oldMovieName, newMovieID, newMovieName));
                    } else {
                        metaData.getCustomer().cancelMovieTickets(customerID, newMovieID, newMovieName,
                                numberOfTickets);
                        response = Message.getErrorMessage("Something went wrong!");
                    }
                } else {
                    response = Message.getErrorMessage("Something went wrong!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        Message response;
        Actions actions = new Actions(metaData);
        if (metaData.isLocalServer(movieID)) {
            response = Message.generateMessageFromString(
                    actions.addMovieTicketsByLocal(customerID, movieID, movieName, numberOfTickets));
        } else {
            try {
                response = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.getAddMovieTicketsCommand(customerID, movieID, movieName, numberOfTickets),
                        Role.getLocationPrefix(movieID)));
            } catch (Exception e) {
                response = Message.getErrorMessage("Something went wrong, please try again later.");
            }
        }
        return response.getMessage();
    }

    @Override
    public String getBookingSchedule(String customerID) {
        ArrayList<String> response = new ArrayList<>();
        Message message;
        Actions actions = new Actions(metaData);
        try {
            String localResponse = Message
                    .generateMessageFromString(
                            actions.getBookingSchedule(Commands.getBookingScheduleCommand(customerID)))
                    .extractMessage();
            if (!localResponse.equals("")) {
                response.add(localResponse);
            }
            for (String prefix : ReplicaManager.locationPorts.keySet()) {
                if (prefix.toLowerCase().equals(metaData.getPrefix().toLowerCase())) {
                    continue;
                } else {
                    // SEND COMMAND TO REMOTE SEVER BASED ON PREFIX AND GET RESPONSE
                    Message message1 = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                            Commands.getBookingScheduleCommand(customerID), Role.getLocationPrefix(prefix)));
                    String remoteResponse = message1.extractMessage();
                    if (!remoteResponse.equals("")) {
                        response.add(remoteResponse);
                    }
                }
            }
            message = Message.getSuccessMessage(String.join(Commands.DELIMITER, response));
        } catch (Exception e) {
            e.printStackTrace();
            message = Message.getErrorMessage(e.getLocalizedMessage());
        }
        return message.getMessage().replace("SUCCESS::","");
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        Message response;
        Actions actions = new Actions(metaData);
        if (metaData.isLocalServer(movieID)) {
            response = Message.generateMessageFromString(
                    actions.cancelMovieTicketsByLocal(customerID, movieID, movieName, numberOfTickets));
        } else {
            try {
                response = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.getCancelMovieTicketsCommand(customerID, movieID, movieName, numberOfTickets),
                        Role.getLocationPrefix(movieID)));
            } catch (Exception e) {
                response = Message.getErrorMessage("Something went wrong, please try again later.");
            }
        }
        return response.getMessage();
    }
}
