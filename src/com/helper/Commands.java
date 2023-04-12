/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.helper;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Commands {
    public static final String INT_SERVER = "INT_SERVER";
    public static final String AKG_INIT_SERVER = "AKG_INIT_SERVER";
    public static final String READY_TO_EXECUTE = "READY_TO_EXECUTE";
    public static final String GET_LIST_OF_REQUESTS = "GET_LIST_OF_REQUESTS";
    public static final String HEART_BEAT = "HEART_BEAT";
    public static final String AKG_HEART_BEAT = "AKG_HEART_BEAT";
    public static final String RESTART_SERVER_REPLICA = "RESTART_SERVER_REPLICA";
    public static final String ADD_MOVIE_SLOT = "ADD_MOVIE_SLOT";
    public static final String REMOVE_MOVIE_SLOT = "REMOVE_MOVIE_SLOT";
    public static final String LIST_MOVIE_AVAILABILITY = "LIST_MOVIE_AVAILABILITY";
    public static final String BOOK_MOVIE_TICKET = "BOOK_MOVIE_TICKET";
    public static final String GET_CUSTOMER_SCHEDULE = "GET_CUSTOMER_SCHEDULE";
    public static final String CANCEL_MOVIE_TICKET = "CANCEL_MOVIE_TICKET";
    public static final String GET_AVAILABLE_SEATS_BY_SLOT_ID = "GET_AVAILABLE_SEATS_BY_SLOT_ID";
    public static final String EXCHANGE_BOOKED_TICKET = "EXCHANGE_BOOKED_TICKET";
    public static final String GET_CX_BOOKED_SEATS_FOR_SLOT_ID = "GET_CX_BOOKED_SEATS_FOR_SLOT_ID";
    public static final String SEND_ERROR = "SEND_ERROR";
    public static final String DELIMITER = ",";

    public static String generateCommandFromParams(String[] args) {
        return String.join(DELIMITER, args);
    }

    public static String[] generateParamsFromCommand(String command) {
        return command.split(DELIMITER);
    }

    // SERVER COMMANDS
    public static final String getInitReplicaCommand(int port) throws UnknownHostException {
        String hostAddress = Inet4Address.getLocalHost().getHostAddress();
        String[] args = new String[] { INT_SERVER, hostAddress, String.valueOf(port) };
        return Commands.generateCommandFromParams(args);
    }
    public static final String getGetListOfRequestsCommand(){
        String[] args = new String[] { GET_LIST_OF_REQUESTS };
        return Commands.generateCommandFromParams(args);
    }
    public static final String getReadyToExecuteCommand(int port) throws UnknownHostException {
        String hostAddress = Inet4Address.getLocalHost().getHostAddress();
        String[] args = new String[] { READY_TO_EXECUTE, hostAddress, String.valueOf(port) };
        return Commands.generateCommandFromParams(args);
    }
    public static final String getHeartBeatCommand(int port) throws UnknownHostException {
        String hostAddress = Inet4Address.getLocalHost().getHostAddress();
        String[] args = new String[] { HEART_BEAT, hostAddress, String.valueOf(port) };
        return Commands.generateCommandFromParams(args);
    }
    public static final String getAkgHeartBeatCommand() throws UnknownHostException {
        String[] args = new String[] { AKG_HEART_BEAT};
        return Commands.generateCommandFromParams(args);
    }

    public static final String getAKFReplicaCommand() {
        String[] args = new String[] { AKG_INIT_SERVER };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getRestartReplicaCommand(String hostAddress, int port) {
        String[] args = new String[] { "-1",RESTART_SERVER_REPLICA, hostAddress, String.valueOf(port) };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getErrorCommand(String errorMessage) {
        String[] args = new String[] { SEND_ERROR, errorMessage };
        return Commands.generateCommandFromParams(args);
    }

    // ADMIN COMMANDS

    public static final String getRemoveMovieSlotCommand(String movieID, String movieName) {
        String[] args = new String[] { REMOVE_MOVIE_SLOT, movieID, movieName };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getListMovieShowsAvailabilityCommand(String movieName) {
        String[] args = new String[] { LIST_MOVIE_AVAILABILITY, movieName };
        return Commands.generateCommandFromParams(args);
    }

    // CUSTOMER COMMANDS
    public static final String getBookingScheduleCommand(String customerId) {
        String[] args = new String[] { GET_CUSTOMER_SCHEDULE, customerId };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getAddMovieTicketsCommand(String customerID, String movieID, String movieName,
            int numberOfTickets) {
        String[] args = new String[] { BOOK_MOVIE_TICKET, customerID, movieID, movieName,
                String.valueOf(numberOfTickets) };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getCancelMovieTicketsCommand(String customerID, String movieID, String movieName,
            int numberOfTickets) {
        String[] args = new String[] { CANCEL_MOVIE_TICKET, customerID, movieID, movieName,
                String.valueOf(numberOfTickets) };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getAvailableSeatsByMovieCommand(String movieID, String movieName) {
        String[] args = new String[] { GET_AVAILABLE_SEATS_BY_SLOT_ID, movieID, movieName };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getExchangeTicketCommand(String customerID, String oldMovieName, String movieID,
            String newMovieID, String newMovieName, int numberOfTickets) {
        String[] args = new String[] { EXCHANGE_BOOKED_TICKET, customerID, oldMovieName, movieID, newMovieID,
                newMovieName, String.valueOf(numberOfTickets) };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getCustomerSeatsByMovieSlotCommand(String customerID, String movieID, String movieName) {
        String[] args = new String[] { GET_CX_BOOKED_SEATS_FOR_SLOT_ID, customerID, movieID, movieName };
        return Commands.generateCommandFromParams(args);
    }

    public static final String getAddMovieSlotsQuery(String movieID, String movieName, int bookingCapacity) {
        String[] args = new String[] { ADD_MOVIE_SLOT, movieID, movieName, String.valueOf(bookingCapacity) };
        return Commands.generateCommandFromParams(args);
    }

    public static final String removeMovieSlots(String movieID, String movieName) {
        String[] args = new String[] { REMOVE_MOVIE_SLOT, movieID, movieName };
        return Commands.generateCommandFromParams(args);
    }
}
