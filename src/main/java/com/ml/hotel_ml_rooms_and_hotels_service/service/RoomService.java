package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.RoomDto;
import com.ml.hotel_ml_rooms_and_hotels_service.exceptions.ErrorWhileEncodeException;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.RoomMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.RoomRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.EncryptorUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RoomService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final EncryptorUtil encryptorUtil;
    private final KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = "create_room_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    void createRoom(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            if (hotelRepository.findAll().isEmpty()) {
                logger.severe("There is no hotel to add rooms!");
                sendRequestMessage("Error:There is no hotel to add rooms!", messageId, "error_request_topic");
            } else if(hotelRepository.findByName(jsonMessage.optString("hotel")) == null) {
                logger.severe("Hotel with such a name does not exist!");
                sendRequestMessage("Error:Hotel with such a name does not exist!", messageId, "error_request_topic");
            } else if (!roomRepository.findAll().isEmpty() && hotelRepository.findByName(jsonMessage.getString("hotel")).getRooms().stream().anyMatch(room -> room.getNumber() == jsonMessage.getLong("number"))) {
                logger.severe("In this hotel already exist room with this number!");
                sendRequestMessage("Error:In this hotel already exist room with this number!", messageId, "error_request_topic");
            } else {
                Room room;
                RoomDto roomDto = RoomDto.builder()
                        .number(jsonMessage.getLong("number"))
                        .description(jsonMessage.getString("description"))
                        .weekPrice(jsonMessage.optDouble("weekPrice"))
                        .weekendPrice(jsonMessage.optDouble("weekendPrice"))
                        .numberOfBeds(jsonMessage.optLong("numberOfBeds"))
                        .build();
                if (jsonMessage.optString("status").isEmpty()) {
                    roomDto.setStatus(RoomStatus.OK);
                } else {
                    roomDto.setStatus(RoomStatus.valueOf(json.optString("status")));
                }
                room = RoomMapper.Instance.mapRoomDtoToRoom(roomDto);
                room.setHotel(hotelRepository.findByName(jsonMessage.getString("hotel")));
                roomRepository.save(room);
                logger.info("Room was addedd: " + room);
                sendRequestMessage("Room Successfully added!", messageId, "success_request_topic");
            }

        } catch (Exception e) {
            logger.severe("Error while creating room: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "check_room_reservation_price_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    void checkRoomPrice(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            Room room = roomRepository.findByHotelNameAndHotelCityAndNumber(jsonMessage.optString("hotelName"), jsonMessage.optString("hotelCity"), jsonMessage.optLong("roomNumber"));
            double price;
            LocalDate startDate = LocalDate.parse(jsonMessage.optString("startDate"));
            LocalDate endDate = LocalDate.parse(jsonMessage.optString("endDate"));
            Double weekPrice = room.getWeekPrice();
            Double weekendPrice = room.getWeekendPrice();
            Map<String, Integer> days = clarifyDayOfWeek(startDate, endDate);
            price = (days.get("weekends") * weekendPrice + days.get("weekdays") * weekPrice);
            if (room == null) {
                sendRequestMessage("Error:Here will be error handler!", messageId, "error_request_topic");
            } else {
                sendEncodedMessage(String.valueOf(price), messageId, "room_price_topic");
            }

        } catch (Exception e) {
            logger.severe("Error while checking price!: " + e.getMessage());
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

    private Room findRoomByNumber(long number) {
        for (Room room : roomRepository.findAll()) {
            if (room.getNumber() == number) {
                return room;
            }
        }
        return null;
    }

    private Map<String, Integer> clarifyDayOfWeek(LocalDate startDate, LocalDate endDate) {
        int weekdays = 0;
        int weekends = 0;
        Map<String, Integer> map = new HashMap<>();
        Stream<LocalDate> dateStream = startDate.datesUntil(endDate.plusDays(1));
        Iterator<LocalDate> dateIterator = dateStream.iterator();
        while (dateIterator.hasNext()) {
            LocalDate currentDate = dateIterator.next();
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                weekends++;
            } else {
                weekdays++;
            }
        }
        map.put("weekdays", weekdays);
        map.put("weekends", weekends);
        return map;
    }

    private String sendEncodedMessage(String message, String messageId, String topic) {
        try {
            JSONObject json = new JSONObject();
            json.put("messageId", messageId);
            if (message != null) {
                switch (message) {
                    case String s when s.contains("[") -> json.put("message", new JSONArray(s));
                    case String s when s.contains("{") -> json.put("message", new JSONObject(s));
                    default -> json.put("message", message);
                }
            }
            String encodedMessage = encryptorUtil.encrypt(json.toString());
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, encodedMessage);
            future.whenComplete((result, exception) -> {
                if (exception != null) logger.severe(exception.getMessage());
                else logger.info("Message send successfully!");
            });
            return message;
        } catch (Exception e) {
            throw new ErrorWhileEncodeException();
        }
    }

}
