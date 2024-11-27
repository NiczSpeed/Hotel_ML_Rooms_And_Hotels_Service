package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.exceptions.ErrorWhileSaveHotelsException;
import com.ml.hotel_ml_rooms_and_hotels_service.exceptions.ErrorWhileSaveRoomException;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class InitDataService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    Logger logger = Logger.getLogger(getClass().getName());

    @PostConstruct
    private void initData() {
        try {
            initHotels();
            initRooms();
        } catch (ErrorWhileSaveHotelsException | ErrorWhileSaveRoomException e) {
            logger.warning(e.getMessage());
        }
    }


    protected void initHotels() {
        if (hotelRepository.count() == 0) {
            Hotel santaHotel = Hotel.builder()
                    .name("Santa Hotel")
                    .address("Podmorksa 12")
                    .city("Wrocław")
                    .state("Dolnośląskie")
                    .numberOfStars(5)
                    .contact("123456789")
                    .build();
            Hotel newDayHotel = Hotel.builder()
                    .name("New Day Hotel")
                    .address("Obokmorksa 11")
                    .city("Katowice")
                    .state("Śląskie")
                    .numberOfStars(4)
                    .contact("345678537")
                    .build();
            Hotel californiaHotel = Hotel.builder()
                    .name("California Hotel")
                    .address("Kalifornijska 3")
                    .city("Bydgoszcz")
                    .state("Kujawsko-pomorskie")
                    .numberOfStars(3)
                    .contact("765897543")
                    .build();

            hotelRepository.saveAll(List.of(santaHotel, newDayHotel, californiaHotel));
        }
    }


    protected void initRooms() {
        if (roomRepository.count() == 0) {
            Room santaHotel1= Room.builder()
                    .number(1L)
                    .numberOfBeds(4L)
                    .status(RoomStatus.OK)
                    .weekPrice(334.00)
                    .weekendPrice(550.00)
                    .description("First room in our Santa Hotel.")
                    .hotel(hotelRepository.findByName("Santa Hotel"))
                    .build();
            Room santaHotel2= Room.builder()
                    .number(2L)
                    .numberOfBeds(2L)
                    .status(RoomStatus.OK)
                    .weekPrice(270.00)
                    .weekendPrice(350.00)
                    .description("Second room in our Santa Hotel.")
                    .hotel(hotelRepository.findByName("Santa Hotel"))
                    .build();
            Room santaHotel3= Room.builder()
                    .number(3L)
                    .numberOfBeds(1L)
                    .status(RoomStatus.OK)
                    .weekPrice(250.00)
                    .weekendPrice(300.00)
                    .description("Third room in our Santa Hotel.")
                    .hotel(hotelRepository.findByName("Santa Hotel"))
                    .build();
            Room roomNewDayHotel1 = Room.builder()
                    .number(1L)
                    .numberOfBeds(3L)
                    .status(RoomStatus.OK)
                    .weekPrice(123.00)
                    .weekendPrice(320.99)
                    .description("First room in our New Day Hotel.")
                    .hotel(hotelRepository.findByName("New Day Hotel"))
                    .build();
            Room roomNewDayHotel2 = Room.builder()
                    .number(2L)
                    .numberOfBeds(2L)
                    .status(RoomStatus.TEMPORARILY_UNAVAILABLE)
                    .weekPrice(150.00)
                    .weekendPrice(220.99)
                    .description("Second room in our New Day Hotel.")
                    .hotel(hotelRepository.findByName("New Day Hotel"))
                    .build();
            Room roomNewDayHotel3 = Room.builder()
                    .number(3L)
                    .numberOfBeds(1L)
                    .status(RoomStatus.OK)
                    .weekPrice(111.35)
                    .weekendPrice(199.00)
                    .description("Third room in our New Day Hotel.")
                    .hotel(hotelRepository.findByName("New Day Hotel"))
                    .build();
            Room roomCaliforniaHotel1 = Room.builder()
                    .number(1L)
                    .numberOfBeds(2L)
                    .status(RoomStatus.OK)
                    .weekPrice(80.00)
                    .weekendPrice(120.00)
                    .description("First room in our California Hotel.")
                    .hotel(hotelRepository.findByName("California Hotel"))
                    .build();
            Room roomCaliforniaHotel2 = Room.builder()
                    .number(2L)
                    .numberOfBeds(3L)
                    .status(RoomStatus.OK)
                    .weekPrice(100.00)
                    .weekendPrice(170.00)
                    .description("Second room in our California Hotel.")
                    .hotel(hotelRepository.findByName("California Hotel"))
                    .build();
            Room roomCaliforniaHotel3 = Room.builder()
                    .number(3L)
                    .numberOfBeds(1L)
                    .status(RoomStatus.OK)
                    .weekPrice(50.00)
                    .weekendPrice(90.00)
                    .description("Third room in our California Hotel.")
                    .hotel(hotelRepository.findByName("California Hotel"))
                    .build();


            roomRepository.saveAll(List.of(santaHotel1, santaHotel2, santaHotel3));
            roomRepository.saveAll(List.of(roomNewDayHotel1, roomNewDayHotel2, roomNewDayHotel3));
            roomRepository.saveAll(List.of(roomCaliforniaHotel1, roomCaliforniaHotel2, roomCaliforniaHotel3));

        }

    }

}
