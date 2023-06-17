
# Overview

Distributed Systems are a network of computers that communicate with
each other and appear to be as a single coherent system. Each host
executes components and operates a distribution middleware, which
enables the components to coordinate their activities in such a way that
the user perceives the system as a single, integrated computing
facility.

These systems have to be designed in such a way that they are highly
available. In case of a failure or a crash, they should still be
available to end users and recover up from the failure in no time. The
main aim of this project is to develop a highly reliable distributed
system that is highly available and software failure tolerant.

# Architecture

In this project we use active replication wherein FE multicasts each
client request to a failure free sequencer. In the below system,
potentially a number of clients will send requests synchronously as a
web service to the Front End. The Front End will reliably send that
request to a failure free sequencer.

![architecture](/docs/ARCH.png)

The sequencer will attach a unique id and sequence number to the request
and n-unicast or basic multicast it to a group of RMs. Each RM will
execute that request and send the reply back to FE which will then send
a reply to the client. All the RMs will process each client request
identically and reply. Such an architecture requires totally ordered
reliable multicast or n-unicast so that all the RMs perform the same
operations in the same order. Such a system is fault tolerant because
the RMs are independent of each other. Failure detection and recovery is
triggered by the FE.

A replica receives client requests with sequence numbers and FE
information from the sequencer, executes the client requests in total
order according to sequence numbers and sends the results back to the
FE. If FE detects any failure, it’ll trigger a request to restart the
replica again. FE will detect failure by comparing the response from all
the replicas and send the response to client which was sent by majority
of the replicas. FE will also implement dynamic timeout in order to
tackle network failures.

## Client

This part of the system will let user to interact directly with the
system using CLI platform.Here multiple functions can be performed based
on the selected options for admin and customer which are as given
below:  

**Admin**:

-   Add movie slot

-   Remove movie slot

-   List movie availability

-   Book movie tickets

-   Get booking schedule

-   Cancel movie tickets

-   Exchange movie

**Customer**:

-   Book Ticket

-   Get Booking Schedule

-   Cancel Ticket

-   Exchange Ticket

  
Client CLI will use web services provided by front-end to execute the
request provided by CLI.As web services are used to make the API for the
system, it will make the system more efficient and scalable to handle
more requests parallel. For accessing web services provided by the
front-end, two end points are used to access all the functionalities.

## Front End Services

Front end store all the information about how many replica are available
and which of them are active at the moment.Also, it has the records of
which replica is consistent in responding all the requests and also
store the data about which is truly working as expected. This part of
the system handle all the requests from the clients as a web service
requests.It then convert all the requests using string manipulation and
forward that to the sequencer using UDP socket.Once the front-end send
the request to sequencer, it will then wait for the responses directly
from the replica itself.Once two or more responses are gathers it will
reply back to client with the response.

If the response from any of the replica are not aligned with other
responses, it will considered as a negative response, once three
negative response are received by any of the replica, it will trigger
restart procedure where front end will send a request to the replica
manager that it need to be restarted in order to work
correctly.Meanwhile the replica will not get any requests to execute, on
the other hand, once the replica is up and running, it will send a
message that it is ready to handle request.Once front-end acknowledge
replica status, it will add that replica in the pool.

Also, if front-end does not receive any response within given period of
time, it will cause a trigger to setup new replica.Here we are deploying
the system on a local network, so it has been assumed that the packet
loss is near to zero.In case, replica server crashes, it will not
respond back to front-end to trigger this scenario.

## Failure Free Sequencer

The main task of the sequencer is to make sure the client request is
processed in total order. The Frontend will send a request to the
sequencer and the sequencer will assign each request a sequence id and
send the request with that sequence id to the replica managers using
reliable n-unicast or multicast, the replica manager will ensure that
the requests are processed in a sequential manner based on the sequence
id received from the sequencer. The sequencer process needs to be
failure free because if there is any failure in the sequencing of the
request the system will not be consistent.

### Total Order

If a correct process delivers message m before it delivers m’, then any
other correct process that delivers m’ will deliver m before m’. In
total order broadcast, the processes must deliver all messages according
to the same order (i.e., the order is now total) Note that this order
does not need to respect causality (or even FIFO ordering) Total order
broadcast can be made to respect causal (or FIFO) ordering. A replicated
service where the replicas need to treat the requests in the same order
to preserve consistency

### Multi Cast

The Multicast Datagram Socket class is a valuable tool for transmitting
and receiving IP multicast packets. It is an extension of the
DatagramSocket class that enables the joining of "groups" of other
multicast hosts on the internet. A MulticastSocket functions as a UDP
DatagramSocket with extra features that allow it to communicate with
multiple hosts simultaneously. These groups are defined by a class IP
address and a standard UDP port number, which enable efficient
transmission of data to many recipients at once.

### N-Unicast

N-Unicast is a communication paradigm that allows one-to-many
communication, where a single sender can communicate with multiple
receivers simultaneously, but each receiver receives a unique copy of
the message. Java supports N-Unicast through the MulticastSocket class,
which provides additional capabilities for joining **groups** of other
multicast hosts on the internet.

## Replica Manager

Replica manager is responsible for detecting system crash on real-time
systems. If say,while executing one of the requests, the system failed,
the replica manager will restart itself, using another replica state.It
will automatically handle the system crash problem by replicating the
same state as another replica.

Replica Manager will receive all the commands by UDP socket, and decode
them.After decoding it will connect with the different servers such as
Atwater, Outremont or Verdun.This connection will be made using web
services or web services provided by each server instance.It will then
reply back to front-end with the correct response provided by the
replica server.

## Replica Server

Replica Server is where all the requests will be executed. In this
system, each replica will have three different servers based on each
location such as Atwater, Outremont or Verdun. Each server will provide
services by web service implementation, which is reliable and scalable.
It also supports multi-threading implicitly.

The request will be handled by the location of the user, for example, if
the username is atwa1189, then it will be handled by the atwater server.
In case the user wants to book tickets at different location, then the
internal communication between server will be done using UDP protocol.
As we are deploying the system on local server, it is assumed that the
packet loss is near to zero, so UDP communication can be considered as
reliable.

# Class Diagram

## Client

Here, Menu class will handle all the UI related functionalities such as
how to represent all the responses we are getting from the front-end
services such as listing movie availability and tickets.On the other
hand, AdminAPI and CustomerAPI will handle all the functions based on
the role provided by the user, both classes will have an instance of
Logger, which will allow them to keep track all the actions performed by
the instance of the client component.

## Front End Services

ReplicaMetadata is used to store all the information regarding replica
such as replica IP address and port.Once the replica is started, it will
register itself with the central repository located at front end.Here,
AdminImpl and CustomerImpl is implemented using Admin and Customer
interfaces which is used to provide web services by front end.Both
AdminImpl and CustomerImpl will receive a web service request from
client and convert it into a string with specific format which will be
decoded at the replica and executed. In the first half, front end will
send the command encoded into string to sequencer and then sequencer
will attach a total order number and forward to each replica manager.

# Test Cases

A test case is a specific set of inputs, conditions, and expected
outcomes designed to test the functionality of a software application.
It is a critical part of software testing, as it helps identify and
isolate defects in the software. A test case typically includes
information such as test data, preconditions, and expected results. Test
cases are designed to cover all possible scenarios and ensure that the
software application performs as intended. By executing test cases,
software testers can verify that the software meets the requirements and
functions correctly. Properly designed test cases can help improve the
quality and reliability of software applications.

For this system we have provided following test cases to make sure that
the system is working correctly.  
  
**Basic Operations:**

|**Test Case Type** | **Description** | **Test Step**} | **Status** |
|---|---|---|---|
| Adding Movie Slot | Add a movie slot with movie name and movie id | If a movie already exists, booking capacity will get updated, if not new slot will be created in the data structure | PASS |
| Book a ticket/s and maintain booking count |Customer can book a ticket/s in any the- atre and the server will maintain a book- ing count |Customer information is stored in the data structure and booking count is updated from the particular movie | PASS |
| Booking tickets in other areas | Customer can book only at most 3 tickets in other areas in a week | UDP requests are sent to other area’s servers to check the booking and book at most 3 tickets in a week | PASS |
| Create movie slot from one week | Admin can create movie shows for next one week from current date |  | PASS |
| Creating log files | Each server haves its own log file | Logger class is used to create log files that stores all the information of each operation performed by Admin and Client | PASS |
| Removing movie slots | Remove movie slots from the server | Hashmap is updated by removing a particular movie slot using movieId | PASS |
| Lists movie shows | Lists all the movie shows available from all the servers | Access the data structure to get the list of movie shows available for a particular movie | PASS |
| Get Booking Schedule | Lists information about the movie tickets booked | Access the data structure to output required information | PASS |
| Cancel Movie Tickets | Cancels the tickets or customer information | Hashmap is updated to cancel the movie tickets purchased by the customer | PASS |
| Concurrency | All the servers can work at the same time |  | PASS |
| Multiple Users | Multiple users can access the server at the same time |  | PASS |
| Exchange Tickets | Exchanging tickets of old movie show with new movie show | Two functions are used: cancelling given number of tickets and adding/ booking tickets for the new show (if available) | PASS |

**Failure Handling Operations:**

 **Test Case Type** | **Description** | **Test Step** | **Status**
---|---|---|---
 Failure Detection and Recovery at Replica | Each replica should have replica manager that detects and recovers from failure |  | PASS 
 Implementing n-Unicast | Sequencer n-unicasts the message the replica managers | Sequencer assigns a unique sequence–id and sends the request that came from front-end to the replica managers | PASS 
 Handling Software Failure | FE compares the results from all the replicas | FE sends back the result to client as soon as two correct identical responses are received | PASS 
 Replacing the replica | Replace the replica if it produces incorrect results | Restart and then replace the replica if it produces incorrect results for three consecutive times | PASS 
 Timeout | Timeout the replica | FE waits for 1s and restarts the replica | PASS 
 Removing movie slots | Remove movie slots from the server | Hashmap is updated by removing a particular movie slot using movieId | PASS 
