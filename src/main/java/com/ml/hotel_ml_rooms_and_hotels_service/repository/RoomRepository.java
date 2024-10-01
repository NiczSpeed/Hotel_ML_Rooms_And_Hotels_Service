package com.ml.hotel_ml_rooms_and_hotels_service.repository;

import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Room findByNumber(Long number);
    Room findByHotelName(String hotelName);
    Room findByHotelNameAndHotelCityAndNumber(String hotelName, String hotelCity, Long roomNumber);

}
