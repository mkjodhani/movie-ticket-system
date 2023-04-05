package com.frontend.services.impl;

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
                    newMovieName, numberOfTickets);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.wait();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            String command = Commands.getAddMovieTicketsCommand(customerID, movieID, movieName, numberOfTickets);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.wait();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String getBookingSchedule(String customerID) {
        try {
            String command = Commands.getBookingScheduleCommand(customerID);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.wait();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            String command = Commands.getListMovieShowsAvailabilityCommand(movieName);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.wait();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            return Commands.getErrorCommand(e.getMessage());
        }
    }
}
