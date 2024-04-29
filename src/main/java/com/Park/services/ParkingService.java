package com.Park.services;

import com.Park.entities.User;
import com.Park.controllers.dto.ParkingDto;
import com.Park.entities.Parking;

import java.util.List;

public interface ParkingService {

    List<Parking> findAllParkingsByStatus(String status);

    Parking findById(int theId);

    List<Parking> findByUser(User user);

    List<Parking> findAllAccepted();

    List<Parking> findAllAcceptedByUser(User user);

    List<Parking> findByMaxPrice(String price);

    Parking update(ParkingDto parkingDto);

    //AICI
    Parking save(ParkingDto theParking);

    Parking updateStatus(ParkingDto parkingDto);

    void delete(int theId);

    void delete(Parking parking);

    List<Parking> findAllByTheCity(String city);

    List<Parking> findAllByTheCountry(String country);


}
