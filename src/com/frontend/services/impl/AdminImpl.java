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
        System.out.println("addMovieSlots");
        try {
            String command = Commands.getAddMovieSlotsCommand(movieID, movieName, bookingCapacity);
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
    public String removeMovieSlots(String movieID, String movieName) {
        try {
            System.out.println("removeMovieSlots");
            String command = Commands.getRemoveMovieSlotCommand(movieID, movieName);
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
    public String listMovieShowsAvailability(String movieName) {
        try {
            System.out.println("listMovieShowsAvailability:"+movieName);
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
