package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.RoomDto;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class HotelService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final HotelRepository hotelRepository;
    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public HotelService(HotelRepository hotelRepository, KafkaTemplate kafkaTemplate) {
        this.hotelRepository = hotelRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "create_hotel_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void createHotel(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            HotelDto hotelDto = new HotelDto(json.optString("name"), json.optString("address"), json.optString("city"), json.optString("state"), json.optInt("numberOfStars"), json.optString("contact"), new HashSet<>());
            Hotel hotel = HotelMapper.Instance.mapHotelDtoToHotel(hotelDto);
            hotelRepository.save(hotel);
            logger.info("Hotel was addedd: " + hotel);
            sendRequestMessage("Hotel Successfully added!", messageId, "success_request_topic");

        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

//    @KafkaListener(topics = "create_hotel_topic", groupId = "hotel_ml_rooms_and_hotels_service")
//    private void createHotel(String message) throws Exception {
//        try {
//            JSONObject json = decodeMessage(message);
//            String messageId = json.optString("messageId");
//            HotelDto hotelDto = new HotelDto();
//            hotelDto.setName(json.optString("name"));
//            hotelDto.setCity(json.optString("city"));
//            hotelDto.setAddress(json.optString("address"));
//            hotelDto.setState(json.optString("state"));
//            hotelDto.setNumberOfStars(json.optInt("numberOfStars"));
//            hotelDto.setContact(json.optString("contact"));
//            hotelRepository.save(HotelMapper.Instance.mapHotelDtoToHotel(hotelDto));
//            logger.info("Hotel was addedd: " + hotelDto);
//            sendRequestMessage("Hotel Successfully added!", messageId, "success_request_topic");
//
//        } catch (Exception e) {
//            logger.severe("Error while creating hotel: " + e.getMessage());
//        }
//    }

    @KafkaListener(topics = "request_free_hotels", groupId = "hotel_ml_rooms_and_hotels_service")
    private void getHotelsByCityAndDateWithFreeRooms(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            Set<HotelDto> freeHotels = new HashSet<>();
            List<HotelDto> hotels = HotelMapper.Instance.mapHotelListToHotelDtoList(hotelRepository.findAll());
            if (!hotels.isEmpty()) {
                for (HotelDto hotel : new HashSet<>(hotels)) {
                    if (hotel.getCity().equals(json.getString("city"))) {
                        freeHotels.add(hotel);
                    }
                }
            }
            String freeHotelsString = freeHotels.toString().replaceAll("HotelDto", "").replaceAll("RoomDto", "");
            logger.info(freeHotelsString);
            String messageWithId = attachMessageId("{hotels:" + freeHotelsString + "}", messageId);
//            sendRequestMessage(String.valueOf(freeHotels.toString()), messageId, "response_free_hotels");
            logger.severe(messageWithId);
            kafkaTemplate.send("response_free_hotels", Base64.getEncoder().encodeToString(messageWithId.getBytes()));
        } catch (Exception e) {
            logger.severe("Error while getting hotels list!  " + e.getMessage());
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

    private String attachMessageId(String message, String messageId) {
        JSONObject json = new JSONObject(message);
        json.put("messageId", messageId);
        return json.toString();
    }

}
