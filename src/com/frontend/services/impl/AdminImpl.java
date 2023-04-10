package com.frontend.services.impl;

import com.frontend.query.FrontEndQuery;
import com.helper.Commands;
import com.shared.Admin;

import javax.jws.WebService;

/**
 * @author mkjodhani
 * @project
 * @since 17/03/23
 */
@WebService(endpointInterface = "com.shared.Admin")
public class AdminImpl implements Admin {
    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        try {
            System.out.println("com.shared.Admin");
            String command = Commands.getAddMovieSlotsQuery(movieID, movieName, bookingCapacity).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.run();
            thread.join();
            String response = frontEndQuery.getQueryResponse();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        try {
            String command = Commands.getRemoveMovieSlotCommand(movieID, movieName).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }

    @Override
    public String listMovieShowsAvailability(String movieName) {
        try {
            String command = Commands.getListMovieShowsAvailabilityCommand(movieName).toUpperCase();
            FrontEndQuery frontEndQuery = new FrontEndQuery(command);
            Thread thread = new Thread(frontEndQuery);
            thread.start();
            thread.join();
            return frontEndQuery.getQueryResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Commands.getErrorCommand(e.getMessage());
        }
    }
}
