/**
 *
 * @project Distributed Movie Ticket Booking System
 * @author Mayur Jodhani
 * @version 1.0.0
 * @since 2023-01-24
 */
package com.client.menu;

import com.helper.Input;

public class Menu {
    private static final int padding = 70;

    public static void mainMenu() {
        while (true) {
            try {
                String userId = Menu.getUserId().toLowerCase();
                if (Input.getRole(userId).equals(Input.USER_TYPE.ADMIN)) {
                    AdminAPI adminAPI = new AdminAPI(userId);
                    adminAPI.start();
                } else if (Input.getRole(userId).equals(Input.USER_TYPE.CUSTOMER)) {
                    CustomerAPI customerAPI = new CustomerAPI(userId);
                    customerAPI.start();
                } else {
                    System.out.println("Please provide valid information");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }

    public static String getUserId() {
        while (true) {
            String userId = Input.getString("Enter the user ID : ");
            if (Input.isValidId(userId)) {
                return userId;
            } else {
                System.out.println("Please provide valid user id.");
            }
        }
    }

    public static String getHorizontalLine() {
        String string = "";
        for (int i = 0; i < Menu.padding; i++) {
            string += "-";
        }
        string += "\n";
        return string;
    }

    public static String getRightPaddingString(String value) {
        return String.format("|%-" + (Menu.padding - 2) + "s|\n", value);
    }

    public static String getLeftPaddingString(String value) {
        return String.format("|%" + (Menu.padding - 2) + "s|\n", value);
    }
}
