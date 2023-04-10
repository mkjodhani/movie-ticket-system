package com.replica.theatre.web;

import com.replica.theatre.TheatreMetaData;
import com.shared.Admin;
import com.shared.Customer;

import javax.xml.ws.Endpoint;

/**
 * @author mkjodhani
 * @version 2.0
 * @project
 * @since 11/03/23
 */
public class Publisher {
    private TheatreMetaData metaData;
    private Endpoint customerEndpoint, adminEndpoint;
    public Publisher(TheatreMetaData metaData) {
        this.metaData = metaData;
    }

    public void publish() {
        String localhost = String.format("http://localhost:%d/ws", metaData.getWebPort());
        Admin admin = new AdminService(metaData);
        Customer customer = new CustomerService(metaData);
        metaData.setAdmin(admin);
        metaData.setCustomer(customer);
        metaData.setEndpoint(localhost);
        adminEndpoint = Endpoint.publish(localhost + "/admin", admin);
        customerEndpoint = Endpoint.publish(localhost + "/customer", customer);
    }
    public void stop(){
        customerEndpoint.stop();
        adminEndpoint.stop();
    }
}
