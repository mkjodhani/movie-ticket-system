/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.replica.theatre.query;

import com.helper.Commands;
import com.replica.manager.ReplicaManager;
import com.replica.theatre.Theatre;
import com.replica.theatre.TheatreMetaData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;

public class Query implements Runnable {
    private DatagramPacket packet;
    private TheatreMetaData metaData;

    public Query(DatagramPacket datagramPacket, TheatreMetaData metaData) {
        this.packet = datagramPacket;
        this.metaData = metaData;
    }

    private String extractCommand() {
        return new String(this.packet.getData(), 0, this.packet.getLength());
    }

    private String executeCommand(String command) {
        Theatre.LOGGER.log(Level.INFO, String.format("Executing UDP command : %s", command));
        String[] args = command.split(Commands.DELIMITER);
        String cmd = args[0];
        String result = "";
        Actions actions = new Actions(metaData);
        switch (cmd) {
            case Commands.AKG_INIT_SERVER:
                result = command.replace(Commands.AKG_INIT_SERVER, Commands.AKG_INIT_SERVER);
                break;
            case Commands.ADD_MOVIE_SLOT:
                result = actions.addMovieSlot(command);
                break;
            case Commands.REMOVE_MOVIE_SLOT:
                result = actions.removeMovieSlot(command);
                break;
            case Commands.LIST_MOVIE_AVAILABILITY:
                result = actions.checkAvailability(command);
                break;
            case Commands.BOOK_MOVIE_TICKET:
                result = actions.addMovieTickets(command);
                break;
            case Commands.GET_CUSTOMER_SCHEDULE:
                result = actions.getBookingSchedule(command);
                break;
            case Commands.CANCEL_MOVIE_TICKET:
                result = actions.cancelMovieTickets(command);
                break;
            case Commands.GET_AVAILABLE_SEATS_BY_SLOT_ID:
                result = actions.getAvailableSeatsByMovie(command);
                break;
            case Commands.GET_CX_BOOKED_SEATS_FOR_SLOT_ID:
                result = actions.getCustomerSeatsByMovieSlot(command);
                break;
        }
        Theatre.LOGGER.log(Level.INFO, String.format("Returning UDP result for %s => %s", command, result));
        return result;
    }

    @Override
    public void run() {
        String result = "";
        try {
            result = executeCommand(extractCommand());
            sendResponse(result);
            Theatre.LOGGER.log(Level.INFO, String.format("Response sent for UDP request =>", result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(String string) throws IOException {
        String response = "";
        if (string != null) {
            response = string;
        }
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket datagramPacket = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(),
                packet.getPort());
        socket.send(datagramPacket);
    }

    public static String executeCommandByServerPrefix(String command, String prefix) throws IOException {
        TheatreMetaData metaData = ReplicaManager.locationPorts.get(prefix);
        if (metaData == null) {
            return "No theatre found for " + prefix;
        } else {
            InetAddress destinationAddress = metaData.getInetAddress();
            int destinationPort = metaData.getPort();
            System.out.println("Sending this command(" + command + ") to " + metaData.getInetAddress().getHostName()
                    + " and the port " + destinationPort);
            // use temp socket to connect with the server
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(command.getBytes(), command.length(), destinationAddress,
                    destinationPort);
            socket.send(datagramPacket);

            byte bytes[] = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(bytes, bytes.length);
            socket.receive(receivedPacket);
            String result = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            System.out.println("Received this result(" + result + ") from "
                    + receivedPacket.getAddress().getHostAddress() + " and the port " + receivedPacket.getPort());
            return result;
        }
    }
}
