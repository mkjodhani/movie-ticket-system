/**
 * The program implements an application that
 * input from users and issues cheques.
 *
 * @author  Mayur Jodhani
 * @version 1.0
 * @since   2023-01-24
 */

package com.helper;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input {
    public static HashMap<Character, Character> slotTypeKeyMap = new HashMap<>();
    static {
        slotTypeKeyMap.put('M', 'A');
        slotTypeKeyMap.put('A', 'E');
        slotTypeKeyMap.put('-', 'M');
        slotTypeKeyMap.put('E', '*');
        slotTypeKeyMap.put('m', 'a');
        slotTypeKeyMap.put('a', 'e');
        slotTypeKeyMap.put('-', 'm');
        slotTypeKeyMap.put('e', '*');
    }

    public enum USER_TYPE {
        ADMIN,
        CUSTOMER
    }

    static private Scanner scanner = new Scanner(System.in);
    static DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private static final Pattern pattern = Pattern.compile("^(\\w{3})([ACac])(\\d{4})$");

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

    private static void prefixInputLabel() {
        System.out.print(">>> ");
    }

    /**
     * takes input from the user
     * 
     * @param label description of the user input
     * @return valid integer value
     */
    public static int getInteger(String label) {
        int input;
        while (true) {
            try {
                System.out.println(label);
                prefixInputLabel();
                input = Integer.parseInt(scanner.nextLine());
                return input;
            } catch (Exception e) {
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     *
     * @param label provide the description of input
     * @param min   provide min value for given input
     * @param max   provide max value for given input
     * @return valid int value
     */
    public static int getIntegerInRange(String label, int min, int max) {
        int input;
        while (true) {
            try {
                System.out.println(label);
                prefixInputLabel();
                input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println(String.format("Please enter the value between %d and %d.", min, max));
                }
            } catch (Exception e) {
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     * takes input from the user
     * 
     * @param label description of the user input
     * @return valid decimal value
     */
    public static double getDouble(String label) {
        double input;
        while (true) {
            try {
                System.out.println(label);
                prefixInputLabel();

                input = Double.parseDouble(scanner.nextLine());
                return input;
            } catch (Exception e) {
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     * takes input from the user
     * 
     * @param label description of the user input
     * @return valid string
     */
    public static String getString(String label) {
        String input;
        while (true) {
            try {
                System.out.println(label);
                prefixInputLabel();
                input = scanner.nextLine().trim();
                return input;
            } catch (Exception e) {
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     * takes input from the user
     * 
     * @param label description of the user input
     * @return valid date
     */
    public static Date getDate(String label) {
        Date date;
        while (true) {
            try {
                System.out.println(label);
                prefixInputLabel();
                date = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
                formatter.parse(formatter.format(date));
                return date;
            } catch (Exception e) {
                System.out.println("Please enter digit as a input for given field : " + e.getMessage());
            }
        }
    }

    public static final boolean isValidId(String id) {
        Matcher matcher = pattern.matcher(id.toLowerCase());
        try {
            if (matcher.find()) {
                String prefix = matcher.group(1);
                if (!"atw-ver-out".contains(prefix)) {
                    return false;
                }
                String userType = matcher.group(2);
                int userId = Integer.parseInt(matcher.group(3));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static final boolean isValidMovieId(String slotId) {
        try {
            // Valid location prefix
            if (!"atw-ver-out".contains(slotId.toLowerCase().substring(0, 3))) {
                return false;
            }
            // valid slot time (Morning-Afternoon-Evening)
            if (!"m,a,e".contains(slotId.toLowerCase().substring(3, 4))) {
                return false;
            }
            int day = Integer.parseInt(slotId.substring(4, 6)), month = Integer.parseInt(slotId.substring(6, 8)),
                    year = Integer.parseInt(slotId.substring(8, 10)) + 2000;
            LocalDate movieDate = LocalDate.of(year, month, day);
            LocalDate today = LocalDate.now();
            long totalDays = ChronoUnit.DAYS.between(today, movieDate);
            return totalDays >= 0 && totalDays <= 6;
        } catch (Exception e) {
            return false;
        }
    }

    public static final String getMovieID() {
        String movieID = "";
        while (true) {
            System.out.println("Enter movie ID :");
            movieID = scanner.nextLine().trim().toLowerCase();
            if (isValidMovieId(movieID)) {
                return movieID;
            } else {
                System.out.println("Please provide valid movie ID.");
            }
        }
    }

    public static USER_TYPE getRole(String id) {
        // based on the 4th char return the role
        if (id.charAt(3) == 'a') {
            return USER_TYPE.ADMIN;
        } else {
            return USER_TYPE.CUSTOMER;
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

    private static final String getNextDateFromID(String slotId) {
        int day = Integer.parseInt(slotId.substring(4, 6)), month = Integer.parseInt(slotId.substring(6, 8)) - 1,
                year = Integer.parseInt(slotId.substring(8, 10)) + 2000;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        Date nextDate = calendar.getTime();
        return String.format("%02d%02d%02d", nextDate.getDate(), nextDate.getMonth() + 1, nextDate.getYear() - 100);
    };

    private static final String getNextSlot(ArrayList<String> slots, String currentSlot) {
        slots.remove(currentSlot);
        if (slots.isEmpty()) {
            return null;
        }
        String location = currentSlot.substring(0, 3);
        ArrayList<String> nextList = new ArrayList<>();
        ArrayList<String> currentList = new ArrayList<>();
        Collections.sort(nextList);
        Collections.sort(currentList);
        char slotType = currentSlot.charAt(3);
        char nextSlotType = slotTypeKeyMap.get(slotType);
        for (String slot : slots) {
            if (slot.contains(currentSlot.substring(4))) {
                if (nextSlotType != '*') {
                    currentList.add(slot);
                }
            } else {
                nextList.add(slot);
            }
        }
        if (currentList.isEmpty()) {
            String nextSlotDate = getNextDateFromID(currentSlot);
            return getNextSlot(nextList, location + "-" + nextSlotDate);
        } else {
            return currentList.get(0);
        }
    }

    public static final String getNextAvailableSlotID(Set<String> slots, String currentSlot) {
        ArrayList<String> listOfSlots = new ArrayList<>();
        for (String slot : slots) {
            listOfSlots.add(slot);
        }
        return getNextSlot(listOfSlots, currentSlot);
    }

    public static Registry getRegistryByPort(int port) {
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (Exception e) {
            try {
                registry = LocateRegistry.getRegistry("localhost", port);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
        return registry;
    }
}
