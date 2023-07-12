package com.airline.reservation.repository;

import com.airline.reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface PassengerRepository extends JpaRepository<Passenger,Long> {

    boolean existsByEmail(String email);
//    List<Passenger> findByFlightId(long flightId);
}
