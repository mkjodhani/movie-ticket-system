package com.frontend.services.impl;

import com.frontend.Frontend;
import com.frontend.query.FrontEndQuery;
import com.helper.Commands;
import com.shared.Customer;

import javax.jws.WebService;

/**
 * @author mkjodhani
 * @since 17/03/23
 */
@WebService(endpointInterface = "com.shared.Customer")
public class CustomerImpl implements Customer {
    @Override
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
            String newMovieName, int numberOfTickets) {
        try {
            String command = Commands.getExchangeTicketCommand(customerID, oldMovieName, movieID, newMovieID,
                    newMovieName, numberOfTickets).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            String response = frontEndQuery.getQueryResponse();
            String id = Commands.generateParamsFromCommand(response)[0];
            Frontend.addCommand(id+Commands.DELIMITER+command);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            String command = Commands.getAddMovieTicketsCommand(customerID, movieID, movieName, numberOfTickets).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            String response = frontEndQuery.getQueryResponse();
            String id = frontEndQuery.queryResponse.split(Commands.DELIMITER)[0];
            Frontend.addCommand(id+Commands.DELIMITER+command);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String getBookingSchedule(String customerID) {
        try {
            String command = Commands.getBookingScheduleCommand(customerID).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            String response = frontEndQuery.getQueryResponse();
            String id = frontEndQuery.queryResponse.split(Commands.DELIMITER)[0];
            Frontend.addCommand(id+Commands.DELIMITER+command);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            String command = Commands.getCancelMovieTicketsCommand(customerID,movieID,movieName,numberOfTickets).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            String response = frontEndQuery.getQueryResponse();
            String id = frontEndQuery.queryResponse.split(Commands.DELIMITER)[0];
            Frontend.addCommand(id+Commands.DELIMITER+command);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }
}
