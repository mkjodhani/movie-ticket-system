package com.shared;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author mkjodhani
 * @version 2.0
 * @project
 * @since 11/03/23
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Admin {

    /**
     * When an admin invokes this method through the server associated with this
     * admin (determined by the unique adminID prefix), attempts to add a movie with
     * the information passed, and inserts the record at the appropriate location in
     * the hash map. The server returns information to the admin whether the
     * operation was successful or not and both the server and the client store this
     * information in their logs. If a movie record with movieID and movieName
     * already exists, bookingCapacity will be updated for that movie record. If a
     * movie does not exist in the database for that movie name, then a new movie
     * slots is added. Log the information into the admin log file.
     * 
     * @param movieID
     * @param movieName
     * @param bookingCapacity
     * @return log String for the admin
     */
    @WebMethod
    String addMovieSlots(String movieID, String movieName, int bookingCapacity);

    /**
     * When invoked by an admin, the server associated with that admin (determined
     * by the unique adminID) searches in the hash map to find and delete the movie
     * for the indicated movieName and movieID. Upon success or failure, a message
     * is returned to the admin and the logs are updated with this information. If a
     * movie slot does not exist, then obviously there is no deletion performed. If
     * a movie show exists and a client has booked that movie ticket, then delete
     * the movie and book the next available movie show (with same movieName) for
     * that client. Apart from this admin cannot delete the movie show that occurred
     * from the before the current date. Log the information into the log file.
     * 
     * @param movieID
     * @param movieName
     * @return log String for the admin
     */
    @WebMethod
    String removeMovieSlots(String movieID, String movieName);

    /**
     * When an admin invokes this method from his/her area’s theatre through the
     * associated server, that area’s theatre server concurrently finds out the
     * number of tickets available for each movie shows in all the servers, for only
     * the given movieName. This requires inter server communication that will be
     * done using UDP/IP messages and the result will be returned to the client. Eg:
     * Avatar: ATWE150623 3, OUTM161223 6, VERE181123 1
     * 
     * @param movieName
     * @return
     */
    @WebMethod
    String listMovieShowsAvailability(String movieName);

}
