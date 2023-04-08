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
            System.out.println("exchangeTickets");
            String command = Commands.getExchangeTicketCommand(customerID, oldMovieName, movieID, newMovieID,
                    newMovieName, numberOfTickets);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            System.out.println("frontEndQuery.getQueryResponse()::"+frontEndQuery.getQueryResponse());
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            System.out.println("bookMovieTickets");
            String command = Commands.getAddMovieTicketsCommand(customerID, movieID, movieName, numberOfTickets);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            System.out.println("frontEndQuery.getQueryResponse()::"+frontEndQuery.getQueryResponse());
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String getBookingSchedule(String customerID) {
        try {
            System.out.println("getBookingSchedule");
            String command = Commands.getBookingScheduleCommand(customerID);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            System.out.println("frontEndQuery.getQueryResponse()::"+frontEndQuery.getQueryResponse());
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            System.out.println("cancelMovieTickets");
            String command = Commands.getListMovieShowsAvailabilityCommand(movieName);
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            System.out.println("frontEndQuery.getQueryResponse()::"+frontEndQuery.getQueryResponse());
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }
}
