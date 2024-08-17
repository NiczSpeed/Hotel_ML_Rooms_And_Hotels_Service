package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

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
            Hotel checkHotel = hotelRepository.findByName(json.optString("name"));
            if (checkHotel != null && checkHotel.getName().equals(json.optString("name"))) {
                sendRequestMessage("Error:Hotel with this name already exist!", messageId, "error_request_topic");
            } else {
                HotelDto hotelDto = new HotelDto(json.optString("name"), json.optString("address"), json.optString("city"), json.optString("state"), json.optInt("numberOfStars"), json.optString("contact"), new HashSet<>());
                Hotel hotel = HotelMapper.Instance.mapHotelDtoToHotel(hotelDto);
                hotelRepository.save(hotel);
                logger.info("Hotel was addedd: " + hotel);
                sendRequestMessage("Hotel Successfully added!", messageId, "success_request_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "request_free_hotels", groupId = "hotel_ml_rooms_and_hotels_service")
    private void getHotelsByCityAndDateWithFreeRooms(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            LocalDate startDate = LocalDate.parse(json.optString("startDate"));
            LocalDate endDate = LocalDate.parse(json.optString("endDate"));
            if (startDate.isAfter(endDate)) {
                sendRequestMessage("Error:Starting date can't be after ending date!", messageId, "error_request_topic");
            } else {
                Set<HotelDto> freeHotels = new HashSet<>();
                List<HotelDto> hotels = HotelMapper.Instance.mapHotelListToHotelDtoList(hotelRepository.findAll());
                if (!hotels.isEmpty()) {
                    for (HotelDto hotel : new HashSet<>(hotels)) {
                        if (hotel.getCity().equals(json.getString("city"))) {
                            freeHotels.add(hotel);
                        }
                    }
                }
                JSONArray jsonArray = new JSONArray(freeHotels);
                sendEncodedMessage(jsonArray.toString(), messageId, "response_free_hotels");
                logger.info("Message was send.");
            }
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

//    private String attachMessageId(String message, String messageId) {
//        JSONObject json = new JSONObject(message);
//        json.put("messageId", messageId);
//        return json.toString();
//    }

    private String sendEncodedMessage(String message, String messageId, String topic) {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        json.put("message", message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, Base64.getEncoder().encodeToString(json.toString().getBytes()));
        future.whenComplete((result, exception) -> {
            if (exception != null) logger.severe(exception.getMessage());
            else logger.info("Message send successfully!");
        });
        return message;
    }

}
