package com.client;

import com.client.menu.AdminAPI;
import com.client.menu.CustomerAPI;
import com.shared.Admin;
import com.shared.Customer;

/**
 * @author mkjodhani
 * @project
 * @since 07/04/23
 */
public class TestClient {
    public static void main(String[] args) {
        Customer customer = CustomerAPI.getCustomerService();
        Admin admin = AdminAPI.getAdminService();
//        System.out.println("-------------------");
//        System.out.println(
//                admin.addMovieSlots("atwm110423","avengers",120)
//        );
//        System.out.println("-------------------");
//        System.out.println(
//                admin.listMovieShowsAvailability("avengers")
//        );
        System.out.println("-------------------");
        System.out.println(
                customer.bookMovieTickets("outc1234","atwm110423","avengers",5)
        );
//        System.out.println("-------------------");
//        System.out.println(
//                customer.getBookingSchedule("outc1234")
//        );
//        System.out.println("-------------------");
    }
}
