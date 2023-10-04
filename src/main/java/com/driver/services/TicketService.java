package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Optional<Train> trainResponse=trainRepository.findById(bookTicketEntryDto.getTrainId());
        Train train=trainResponse.get();

        int totalNumberOfTickets=0;

        List<Ticket> bookedTickets=train.getBookedTickets();

        for(Ticket t : bookedTickets){
            totalNumberOfTickets+=t.getPassengersList().size();
        }

        if(totalNumberOfTickets + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        String[] routes=train.getRoute().split(",");

        List<Passenger> listOfPassenger=new ArrayList<>();
        List<Integer> passengerId=bookTicketEntryDto.getPassengerIds();

        for(Integer id : passengerId){
            listOfPassenger.add(passengerRepository.findById(id).get());
        }

        int a=-1,b=-1;

        for(int i=0;i<routes.length;i++){
            if(bookTicketEntryDto.getFromStation().toString().equals(routes[i])){
                a=i;
            }
        }

        for(int i=0;i< routes.length;i++){
            if(bookTicketEntryDto.getToStation().toString().equals(routes[i])){
                b=i;
            }
        }

        if(a == -1 || b == -1 || b - a < 0){
            throw new Exception("Invalid stations");
        }

        Ticket newTicket=new Ticket();
        newTicket.setPassengersList(listOfPassenger);
        newTicket.setFromStation(bookTicketEntryDto.getFromStation());
        newTicket.setToStation(bookTicketEntryDto.getToStation());

        int fair=0;
        fair=bookTicketEntryDto.getNoOfSeats() * (b-a) * 300;

        newTicket.setTotalFare(fair);
        newTicket.setTrain(train);

        train.getBookedTickets().add(newTicket);
        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats());

        Passenger passenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(newTicket);

        trainRepository.save(train);

       return ticketRepository.save(newTicket).getTicketId();

    }
}
