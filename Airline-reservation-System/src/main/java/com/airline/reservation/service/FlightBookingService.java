package com.airline.reservation.service;

import com.airline.reservation.entity.Flight;
import com.airline.reservation.entity.FlightBooking;
import com.airline.reservation.entity.Passenger;
import com.airline.reservation.model.*;
import com.airline.reservation.repository.FlightBookingRepository;
import com.airline.reservation.repository.FlightRepository;
import com.airline.reservation.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightBookingService {
    @Autowired
    private FlightBookingRepository flightBookingRepository;
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private FlightRepository flightRepository;

    public ResponseEntity createFlightBooking(long passengerId, long flightId) {
        FlightBooking flightBooking = new FlightBooking();
        if (passengerRepository.existsById(passengerId)) {
            if (flightRepository.existsById(flightId)) {
                if (!(flightBookingRepository.existsByPassengerIdAndFlightId(passengerId, flightId))) {
                    Passenger passenger = passengerRepository.findById(passengerId).get();
                    flightBooking.setPassengerId(passengerId);
                    flightBooking.setFirstName(passenger.getFirstName());
                    flightBooking.setLastName(passenger.getLastName());
                    Flight flight = flightRepository.findById(flightId).get();
                    flightBooking.setFlightId(flightId);
                    flightBooking.setFlightName(flight.getFlightName());
                    flightBooking.setSource(flight.getSource() + "-" + flight.getDestination());
                    flightBooking.setDestination(flight.getDestination());
                    flightBooking.setDepartureTime(flight.getDepartureTime());
                    flightBooking.setArrivalTime(flight.getArrivalTime());
                    flightBooking.setFare(flight.getFare());

                    if (flightBookingRepository.existsByFlightId(flightId)) {
                        int lastBookedSeat = flightBookingRepository.getLastBookedSeat(flightId);
                        if (lastBookedSeat < flight.getTotalSeats()) {
                            flightBooking.setBookSeats(lastBookedSeat + 1);
                            flightBookingRepository.save(flightBooking);

                            List<FlightBooking> byFlightId = flightBookingRepository.findByFlightId(flightId);
                            int totalSeats = byFlightId.size();
                            flight.setBookedSeats(totalSeats);
                            flightRepository.save(flight);
                            return new ResponseEntity(new MessageResponse(true, "createFlightBooking Successfully"), HttpStatus.OK);
                        } else {
                            return new ResponseEntity(new MessageResponse(false, "there is no seats for booking"), HttpStatus.NOT_FOUND);
                        }
                    } else {
                        int lastBookedSeat = 0;
                        if (lastBookedSeat < flight.getTotalSeats()) {
                            flightBooking.setBookSeats(lastBookedSeat + 1);
                            flightBookingRepository.save(flightBooking);//only save booking data(allentriesSave about passengers and traveling


                            //update booked seat in flight data
                            List<FlightBooking> byFlightId = flightBookingRepository.findByFlightId(flightId);
                            int totalSeats = byFlightId.size();
                            flight.setBookedSeats(totalSeats);
                            flightRepository.save(flight);//save booked seat data(noOfseatsBooked)
                            return new ResponseEntity(new MessageResponse(true, "createFlightBooking Successfully"), HttpStatus.OK);
                        } else {
                            return new ResponseEntity(new MessageResponse(false, "there is no seats for booking"), HttpStatus.NOT_FOUND);
                        }
                    }
                } else {
                    return new ResponseEntity(new MessageResponse(false, "Passenger already booked flight...!"), HttpStatus.CONFLICT);
                }
            } else {
                return new ResponseEntity(new MessageResponse(false, "flightId doesn't exist"), HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(new MessageResponse(false, "passengerId doesn't exist"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity updateFlightBooking(long oldFlightId, long newFlightId) {

        if (flightRepository.existsById(oldFlightId)) {
            if (flightRepository.existsById(newFlightId)) {
                Flight flight = flightRepository.findById(newFlightId).get();
                List<FlightBooking> byFlightId = flightBookingRepository.findByFlightId(oldFlightId);
                String flightName = flight.getFlightName();
                String arrivalTime = flight.getArrivalTime();
                String departureTime = flight.getDepartureTime();
                String destination = flight.getDestination();
                for (FlightBooking flightBooking : byFlightId) {
                    flightBooking.setFlightName(flightName);
                    flightBooking.setFlightId(newFlightId);
                    flightBooking.setArrivalTime(arrivalTime);
                    flightBooking.setDepartureTime(departureTime);
                    flightBooking.setDestination(destination);
                    flightBookingRepository.save(flightBooking);
                }
                return new ResponseEntity(new MessageResponse(true, "Successfully FlightBooking Updated")
                        , HttpStatus.OK);
            } else {
                return new ResponseEntity(new MessageResponse(false, "FlightId not exist"), HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(new MessageResponse(false, "FlightId not exist"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity deleteFlightBooking(@RequestParam(name = "bookingId") long bookingId) {
        if (flightBookingRepository.existsById(bookingId)) {
            flightBookingRepository.deleteById(bookingId);
            FlightBooking flightBooking = flightBookingRepository.findById(bookingId).get();
            long flightId = flightBooking.getFlightId();
            Flight flight = flightRepository.findById(flightId).get();
            flight.setBookedSeats(flight.getBookedSeats() - 1);
            flightRepository.save(flight);
            return new ResponseEntity(new MessageResponse(true, "Successfully cancelled booking"),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(new MessageResponse(false, "bookingId not exist!!"),
                HttpStatus.NOT_FOUND);

    }

    public ResponseEntity getByFlightBookingId(long bookingId) {
        if (flightBookingRepository.existsById(bookingId)) {
            FlightBooking flightBooking = flightBookingRepository.findById(bookingId).get();
            return new ResponseEntity(new FlightBookingObjectResponse(true, "Successfully get data", flightBooking), HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse(false, "flightBookingId not Exist!!!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity getAllFlightBooking() {
        List<FlightBooking> allflightBooking = flightBookingRepository.findAll();
        if (!allflightBooking.isEmpty()) {
            return new ResponseEntity(new FlightBookingListResponse(true, "Successfully get All Data!!", allflightBooking)
                    , HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse(false, "Not found Data"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity getBookingList() {
        List<Flight> getAllFlights = flightRepository.findAll();
        List<GetFlightDetailsWithPassengerResponse> flightDetailsWithPassengerResponses = new ArrayList<>();
        for (Flight flight : getAllFlights) {
            Flight flightData = flightRepository.findById(flight.getFlightId()).get();
            String flightName = flightData.getFlightName();
            String source = flightData.getSource();
            int bookedSeats = flightData.getBookedSeats();//
            int totalSeats = flightData.getTotalSeats();//only get total seats
            int remainingSeats = totalSeats - bookedSeats;
            List<FlightBooking> bookingDetails = flightBookingRepository.findByFlightId(flight.getFlightId());
            if (!bookingDetails.isEmpty()) {
                GetFlightDetailsWithPassengerResponse getFlightDetailsWithPassengerResponse = new GetFlightDetailsWithPassengerResponse(flight.getFlightId(), flightName, source, bookedSeats, remainingSeats, bookingDetails);
                flightDetailsWithPassengerResponses.add(getFlightDetailsWithPassengerResponse);
            }
        }
        return new ResponseEntity(new MessageResponseOfPassengerList(true, "successfully", flightDetailsWithPassengerResponses), HttpStatus.OK);
//            FlightBooking flightBooking = flightBookingRepository.findById(flightId).get();
//            flightBooking.getFlightId();
//            flightBooking.getFlightName();
//            flightBooking.getSource();
//            Flight flight = flightRepository.findById(flightId).get();
//            int totalSeats = flight.getTotalSeats();
//            int bookedSeats = flight.getBookedSeats();
//            int remainingSeats = totalSeats-bookedSeats;

//            List<Passenger> byFlightId = passengerRepository.findByFlightId(flightId);
//            for (Passenger passenger : byFlightId) {
//                passenger.getPassengerId();
//                passenger.getFirstName();
//                passenger.getLastName();
//                passenger.getGender();
//                passenger.getAge();
//                GetFlightDetailsWithPassengerResponse getFlightDetailsWithPassengerResponse = new GetFlightDetailsWithPassengerResponse(flightId,,remainingSeats);
//
//            }
//        } else {
//            return new ResponseEntity(new MessageResponse(false, "not get data"), HttpStatus.NOT_FOUND);
//        }
    }
}
