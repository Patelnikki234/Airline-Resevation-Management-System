package com.airline.reservation.repository;

import com.airline.reservation.entity.Flight;
import com.airline.reservation.entity.FlightBooking;
import com.airline.reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {

    boolean existsByFlightId(long flightId);

    @Query(value = "select MAX(book_seats) from flight_booking where flight_id=:flight_Id", nativeQuery = true)
    int getLastBookedSeat(@Param("flight_Id") long flight_Id);

    boolean existsByPassengerIdAndFlightId(long passengerId, long flightId);

    //@Query(value = "select * from flight_booking.passenger_id=passengerId AND flight_booking.flight_id=flightId",
    //nativeQuery = true)
    // long getPassengerIdAndFlightId(long passengerId, long flightId);
    List<FlightBooking> findByFlightId(long oldFlightId);




}