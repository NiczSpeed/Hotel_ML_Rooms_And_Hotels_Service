package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.RoomDto;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.RoomMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class RoomService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final KafkaTemplate kafkaTemplate;
    private final EntityManager entityManager;


    @Autowired
    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository, KafkaTemplate kafkaTemplate, EntityManager entityManager) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.entityManager = entityManager;
    }


//    @KafkaListener(topics = "create_room_topic", groupId = "hotel_ml_rooms_and_hotels_service")
//    private void createRoom(String message) throws Exception {
//        try {
//            JSONObject json = decodeMessage(message);
//            String messageId = json.optString("messageId");
//            RoomDto roomDto = new RoomDto();
////            HotelDto hotelDto = HotelMapper.Instance.mapHotelToHotelDto(findHotelByName(json.optString("hotel")));
//            HotelDto hotelDto = HotelMapper.Instance.mapHotelToHotelDto(hotelRepository.findByName(json.getString("hotel")));
//            logger.severe(hotelDto.toString());
//            roomDto.setNumber(json.optInt("number"));
//            roomDto.setDescription(json.optString("description"));
//            roomDto.setPrice(json.optDouble("price"));
//            if (!json.optString("status").equals("FREE") && !json.optString("status").isEmpty()) {
//                roomDto.setStatus(RoomStatus.valueOf(json.optString("status")));
//            } else {
//                roomDto.setStatus(RoomStatus.FREE);
//            }
//            roomDto.setNumberOfBeds(json.optInt("numberOfBeds"));
//            roomDto.setHotelDto(hotelDto);
//            roomRepository.save(RoomMapper.Instance.mapRoomDtoToRoom(roomDto));
//            logger.info("Room was addedd: " + roomDto);
//
//            sendRequestMessage("Room Successfully added!", messageId, "success_request_topic");
//
//        } catch (Exception e) {
//            logger.severe("Error while creating room: " + e.getMessage());
//        }
//    }

    @KafkaListener(topics = "create_room_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    void createRoom(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            if (hotelRepository.findAll().isEmpty()) {
                sendRequestMessage("Error:There is no hotel to add rooms!", messageId, "error_request_topic");
            } else if (!roomRepository.findAll().isEmpty() && hotelRepository.findByName(json.getString("hotel")).getRooms().stream().anyMatch(room -> room.getNumber() == json.getLong("number"))) {
                sendRequestMessage("Error:In this hotel already exist room with this number!", messageId, "error_request_topic");
            } else {
                RoomDto roomDto = new RoomDto();
                Room room;
                roomDto.setNumber(json.optLong("number"));
                roomDto.setDescription(json.optString("description"));
                roomDto.setPrice(json.optDouble("price"));
                roomDto.setNumberOfBeds(json.optInt("numberOfBeds"));
                if (json.optString("status").isEmpty()) {
                    roomDto.setStatus(RoomStatus.OK);
                } else {
                    roomDto.setStatus(RoomStatus.valueOf(json.optString("status")));
                }
                room = RoomMapper.Instance.mapRoomDtoToRoom(roomDto);
                room.setHotel(hotelRepository.findByName(json.getString("hotel")));
                roomRepository.save(room);
                logger.info("Room was addedd: " + room);
                sendRequestMessage("Room Successfully added!", messageId, "success_request_topic");
            }

        } catch (Exception e) {
            logger.severe("Error while creating room: " + e.getMessage());
        }
    }


    private void changeRoomStatus(long roomNumber, RoomStatus newStatus) {
        try {
            RoomDto roomDto = RoomMapper.Instance.mapRoomToRoomDto(findRoomByNumber(roomNumber));
            if (roomDto != null) {
                roomDto.setStatus(newStatus);
                roomRepository.save(RoomMapper.Instance.mapRoomDtoToRoom(roomDto));
            }
        } catch (Exception e) {
            logger.severe("Error while changing room status: " + e.getMessage());
        }
    }


    private String sendRequestMessage(String message, String messageId, String topic) {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        json.put("message", message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json.toString());
        future.whenComplete((result, exception) -> {
            if (exception != null) logger.severe(exception.getMessage());
            else logger.info("Message send successfully!");
        });
        return message;
    }

    private JSONObject decodeMessage(String message) {
        byte[] decodedBytes = Base64.getDecoder().decode(message);
        message = new String(decodedBytes);
        return new JSONObject(message);
    }

    private HotelDto findHotelByName(String name) {
        List<Hotel> hotels = hotelRepository.findAll();
        HotelDto hotelDto;
        for (Hotel hotel : hotels) {
            if (hotel.getName().equals(name)) {
                hotelDto = HotelMapper.Instance.mapHotelToHotelDto(hotel);
                return hotelDto;
            }
        }
        return null;
    }

    private Room findRoomByNumber(long number) {
        for (Room room : roomRepository.findAll()) {
            if (room.getNumber() == number) {
                return room;
            }
        }
        return null;
    }

}
