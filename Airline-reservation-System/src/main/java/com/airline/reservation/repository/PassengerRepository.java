package com.airline.reservation.repository;

import com.airline.reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PassengerRepository extends JpaRepository<Passenger,Long> {

    boolean existsByEmail(String email);
}
