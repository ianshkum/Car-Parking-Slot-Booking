package com.Park.repositories;

import com.Park.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Integer> {

    List<Booking> findAllByUserId(int id);

    List<Booking> findAllByParkingId(int id);

}
