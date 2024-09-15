package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.FreeHotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.RoomDto;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.FreeHotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class HotelService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();

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
            String reservationMessageId = UUID.randomUUID().toString();
            LocalDate startDate = LocalDate.parse(json.optString("startDate"));
            LocalDate endDate = LocalDate.parse(json.optString("endDate"));

            JSONObject reservationDataJson = new JSONObject().append("startDate", startDate).append("endDate", endDate).append("city", json.optString("city"));

            Set<Hotel> hotels = hotelRepository.findByCity(json.getString("city"));
            Set<FreeHotelDto> freeHotelsDto = FreeHotelMapper.Instance.mapHotelSetToFreeHotelDtoSet(hotels);


            filterFreeRooms(freeHotelsDto, reservationDataJson, reservationMessageId);

            JSONArray jsonArray = new JSONArray(freeHotelsDto);
            sendEncodedMessage(jsonArray.toString(), messageId, "response_free_hotels");
            logger.info("Message was send.");

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

    private void filterFreeRooms(Set<FreeHotelDto> freeHotelDtoSet, JSONObject json, String messageId) {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        responseFutures.put(messageId, responseFuture);
//        String messageWithId = attachMessageId(json.toString(), messageId);

        freeHotelDtoSet.forEach(hotel -> hotel.setRooms(
                hotel.getRooms().stream()
                        .filter(room -> {
                            if (room.getStatus().equals(RoomStatus.FREE)) {
                                json.append("hotel", hotel.getName());
                                json.append("room", room.getNumber());
                                JSONObject messageJson = new JSONObject().append("message", json);
                                String messageWithId = attachMessageId(messageJson.toString(), messageId);
                                kafkaTemplate.send("check_reservation_topic", Base64.getEncoder().encodeToString(messageWithId.getBytes()));
                                try {
                                    String response = responseFuture.get(5, TimeUnit.SECONDS);
                                    responseFutures.remove(messageId);
                                    if (response.contains("False")) { // ma byc true
                                        return true;
                                    }
                                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                    logger.severe(e.getMessage());
                                }
                            }
                            return false;
                        }).collect(Collectors.toSet())
        ));
        logger.severe(json.toString());
    }

    @KafkaListener(topics = "boolean_reservation_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void booleanReservation(String message) {
        getRequestMessage(message);
    }

    void getRequestMessage(String message) {
        String messageId = extractMessageId(message);
        CompletableFuture<String> responseFuture = responseFutures.get(messageId);
        if (responseFuture != null) {
            responseFuture.complete(message);
        }
    }

    String extractMessageId(String message) {
        JSONObject json = new JSONObject(message);
        return json.optString("messageId");
    }

    String attachMessageId(String message, String messageId) {
        JSONObject json = new JSONObject(message);
        json.put("messageId", messageId);
        return json.toString();
    }

}
