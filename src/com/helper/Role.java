/**
 * The program implements an application that
 * input from users and issues cheques.
 *
 * @author  Mayur Jodhani
 * @version 1.0
 * @since   2023-01-24
 */
package com.helper;

import java.util.HashMap;

public class Role {
    public static final String CUSTOMER = "CUSTOMER";
    public static final String ADMIN = "ADMIN";
    private static HashMap<String, String> timeSlots = new HashMap<>();
    private static HashMap<String, String> locations = new HashMap<>();
    static {
        // TIME STAMP
        timeSlots.put("m", "Morning");
        timeSlots.put("a", "Afternoon");
        timeSlots.put("e", "Evening");

        // LOCATIONS
        locations.put("atw", "Atwater");
        locations.put("ver", "Verdun");
        locations.put("out", "Outremont");
    }

    public static String getRole(String id) {
        // based on the 4th char return the role
        if (id.charAt(3) == 'a') {
            return ADMIN;
        } else {
            return CUSTOMER;
        }
    }

    public static String getLocationPrefix(String id) {
        // based on the first three char return location
        return id.substring(0, 3);
    }

    public static String getTimeBySlotID(String slotID) {
        return timeSlots.getOrDefault(slotID.substring(3, 4), "Not available");
    }

    public static String getLocationBySlotIS(String slotID) {
        return locations.getOrDefault(slotID.substring(0, 3), "Not available");
    }
}
