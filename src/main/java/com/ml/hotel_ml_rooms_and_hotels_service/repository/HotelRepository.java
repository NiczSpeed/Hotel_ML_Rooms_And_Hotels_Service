package com.ml.hotel_ml_rooms_and_hotels_service.repository;

import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    Hotel findByName(String name);
    Set<Hotel> findByCity(String location);
}
