package com.replica.theatre.web;

import com.helper.Commands;
import com.helper.Message;
import com.helper.Role;
import com.replica.manager.ReplicaManager;
import com.replica.theatre.TheatreMetaData;
import com.replica.theatre.movie.Movie;
import com.replica.theatre.query.Actions;
import com.replica.theatre.query.Query;
import com.shared.Admin;
import javax.jws.WebService;
import java.io.IOException;

/**
 * @author mkjodhani
 * @version 2.0
 * @project
 * @since 11/03/23
 */
@WebService(endpointInterface = "com.shared.Admin")
public class AdminService implements Admin {
    TheatreMetaData metaData;

    public AdminService(TheatreMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        Message response;
        Actions actions = new Actions(metaData);
        try {
            if (metaData.isLocalServer(movieID)) {
                response = Message
                        .generateMessageFromString(actions.addMovieSlotByLocal(movieID, movieName, bookingCapacity));
            } else {
                // SEND COMMAND TO REMOTE SEVER BASED ON PREFIX AND GET RESPONSE
                response = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.getAddMovieSlotsQuery(movieID, movieName, bookingCapacity),
                        Role.getLocationPrefix(movieID)));
            }
        } catch (Exception e) {
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        Message response;
        Actions actions = new Actions(metaData);
        try {
            if (metaData.isLocalServer(movieID)) {
                response = Message.generateMessageFromString(actions.removeMovieSlotByLocal(movieID, movieName));
            } else {
                // SEND COMMAND TO REMOTE SEVER BASED ON PREFIX AND GET RESPONSE
                response = Message.generateMessageFromString(Query.executeCommandByServerPrefix(
                        Commands.removeMovieSlots(movieID, movieName), Role.getLocationPrefix(movieID)));
            }
        } catch (Exception e) {
            response = Message.getErrorMessage(e.getMessage());
        }
        return response.getMessage();
    }

    @Override
    public String listMovieShowsAvailability(String movieName) {
        final String[] result = { "" };
        Message response;
        Thread[] threads = new Thread[ReplicaManager.locationPorts.keySet().size() - 1];
        try {
            Movie movie = metaData.getMovies().get(movieName);
            if (movie == null) {
                // return Message.getErrorMessage(("No movie found by name of " + movieName));
            } else {
                result[0] = Commands.generateCommandFromParams(movie.getSeats());
            }
            int index = 0;
            for (String prefix : ReplicaManager.locationPorts.keySet()) {
                if (prefix.toLowerCase().equals(metaData.getPrefix().toLowerCase())) {
                    continue;
                } else {
                    // SEND COMMAND TO REMOTE SEVER BASED ON PREFIX AND GET RESPONSE
                    threads[index++] = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            String remoteResponse = null;
                            try {
                                Message remoteMessage = Message.generateMessageFromString(Query
                                        .executeCommandByServerPrefix(Commands.getListMovieShowsAvailabilityCommand(movieName),
                                                Role.getLocationPrefix(prefix)));
                                remoteResponse = remoteMessage.extractMessage();
                                if (!remoteResponse.equals("")) {
                                    result[0] += Commands.DELIMITER + remoteResponse;
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    threads[index - 1].start();
                }
            }
            for (Thread t : threads) {
                t.join();
            }
            response = Message.getSuccessMessage(result[0]);
        } catch (Exception e) {
            response = Message.getErrorMessage(e.getLocalizedMessage());
        }
        return response.getMessage();
    }
}
