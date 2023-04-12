package com.client;

import com.client.menu.AdminAPI;
import com.client.menu.CustomerAPI;
import com.shared.Admin;
import com.shared.Customer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author mkjodhani
 * @project
 * @since 07/04/23
 */
public class TestClient {
    public static void main(String[] args) throws IOException {
        Customer customer = CustomerAPI.getCustomerService();
        Admin admin = AdminAPI.getAdminService();
//        System.out.println("-------------------");
//        System.out.println(
//                admin.addMovieSlots("atwm130423","avengers",120)
//        );
        System.out.println("-------------------");
        System.out.println(
                admin.listMovieShowsAvailability("avengers")
        );
//        System.out.println("-------------------");
//        System.out.println(
//                admin.removeMovieSlots("atwm130423","avengers")
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                admin.listMovieShowsAvailability("avengers")
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                customer.bookMovieTickets("outc1234","atwm130423","avengers",5)
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                customer.cancelMovieTickets("outc1234","atwm130423","avengers",5)
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                customer.getBookingSchedule("outc1234")
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                admin.removeMovieSlots("atwm130423","avengers")
//        );
    }
}
