package com.ml.hotel_ml_rooms_and_hotels_service.service;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.FreeHotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.exceptions.ErrorWhileEncodeException;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.FreeHotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.maper.HotelMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.HotelRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.repository.RoomRepository;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.EncryptorUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
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
@RequiredArgsConstructor
public class HotelService {

    private final RoomRepository roomRepository;
    Logger logger = Logger.getLogger(getClass().getName());

    private final Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();

    private final EncryptorUtil encryptorUtil;
    private final HotelRepository hotelRepository;
    private final KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = "create_hotel_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void createHotel(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            Hotel checkHotel = hotelRepository.findByName(jsonMessage.optString("name"));
            if (checkHotel != null && checkHotel.getName().equals(jsonMessage.optString("name"))) {
                sendRequestMessage("Error:Hotel with this name already exist!", messageId, "error_request_topic");
            } else {
                HotelDto hotelDto = HotelDto.builder()
                        .name(jsonMessage.optString("name"))
                        .address(jsonMessage.optString("address"))
                        .city(jsonMessage.optString("city"))
                        .state(jsonMessage.optString("state"))
                        .numberOfStars(jsonMessage.optInt("numberOfStars"))
                        .contact(jsonMessage.optString("contact"))
                        .rooms(new HashSet<>())
                        .build();
                Hotel hotel = HotelMapper.Instance.mapHotelDtoToHotel(hotelDto);
                hotelRepository.save(hotel);
                logger.info("Hotel was addedd: " + hotel);
                sendRequestMessage("Hotel Successfully added!", messageId, "success_request_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "request_free_hotels_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void getHotelsByCityAndDateWithFreeRooms(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            String messageId = json.optString("messageId");
            JSONObject messageJson = json.getJSONObject("message");
            String reservationMessageId = UUID.randomUUID().toString();
            LocalDate startDate = LocalDate.parse(messageJson.optString("startDate"));
            LocalDate endDate = LocalDate.parse(messageJson.optString("endDate"));
            if (LocalDate.parse(messageJson.optString("startDate")).isBefore(LocalDate.now()) || LocalDate.parse(messageJson.optString("endDate")).isBefore(LocalDate.now())) {
                sendRequestMessage("Error:You are trying to pick a date from the past!", messageId, "error_request_topic");
            } else {
//                JSONObject reservationDataJson = new JSONObject().put("startDate", startDate).put("endDate", endDate).put("city", messageJson.optString("city")););
                Set<Hotel> hotels = hotelRepository.findByCity(messageJson.getString("city"));
                Set<FreeHotelDto> freeHotelsDto = FreeHotelMapper.Instance.mapHotelSetToFreeHotelDtoSet(hotels);

                if (hotelRepository.findAll().isEmpty()) {
                    sendRequestMessage("Error:There is no hotel to get list!", messageId, "error_request_topic");
                } else {
                    filterFreeRooms(freeHotelsDto, messageJson, reservationMessageId);
                    JSONArray jsonArray = new JSONArray(freeHotelsDto);
                    sendEncodedMessage(jsonArray.toString(), messageId, "response_free_hotels_topic");
                    logger.info("Message was send.");
                }
            }
        } catch (Exception e) {
            logger.severe("Error while getting hotels list!  " + e.getMessage());
        }
    }

    @KafkaListener(topics = "request_all_hotels_by_city_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void getAllHotelByCity(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            String messageId = json.optString("messageId");
            JSONObject messageJson = json.getJSONObject("message");
            Set<Hotel> hotels = hotelRepository.findByCity(messageJson.getString("city"));
            Set<FreeHotelDto> freeHotelDtos = FreeHotelMapper.Instance.mapHotelSetToFreeHotelDtoSet(hotels);
            Set<String> hotelsByCity = freeHotelDtos.stream().map(FreeHotelDto::getName).collect(Collectors.toSet());
            if (hotelRepository.findAll().isEmpty()) {
                sendRequestMessage("Error:There is no hotel to get list!", messageId, "error_request_topic");
            } else {
                JSONArray jsonArray = new JSONArray(hotelsByCity);
                sendEncodedMessage(jsonArray.toString(), messageId, "response_all_hotels_by_city_topic");
                logger.info("Message was send.");
            }
        } catch (Exception e) {
            logger.severe("Error while getting hotels list!  " + e.getMessage());
        }
    }


    @KafkaListener(topics = "request_all_hotels_cities_topic", groupId = "hotel_ml_rooms_and_hotels_service")
    private void getAllHotelCities(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            String messageId = json.optString("messageId");
            List<Hotel> hotels = hotelRepository.findAll();
            List<HotelDto> hotelDtoList = HotelMapper.Instance.mapHotelListToHotelDtoList(hotels);
            Set<String> hotelsCitiesList = hotelDtoList.stream().map(HotelDto::getCity).collect(Collectors.toSet());

            if (hotelRepository.findAll().isEmpty()) {
                sendRequestMessage("Error:There is no hotel to get list!", messageId, "error_request_topic");
            } else {
                JSONArray jsonArray = new JSONArray(hotelsCitiesList);
                sendEncodedMessage(jsonArray.toString(), messageId, "response_all_hotels_cities_topic");
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

    private void filterFreeRooms(Set<FreeHotelDto> freeHotelDtoSet, JSONObject json, String messageId) {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        responseFutures.put(messageId, responseFuture);
        freeHotelDtoSet.forEach(hotel -> hotel.setRooms(
                hotel.getRooms().stream()
                        .filter(room -> {
                            if (!room.getStatus().equals(RoomStatus.TEMPORARILY_UNAVAILABLE) && room.getNumberOfBeds() >= json.optLong("numberOfBeds")) {
                                json.put("hotel", hotel.getName());
                                json.put("room", room.getNumber());
                                sendEncodedMessage(json.toString(), messageId, "check_reservation_topic");
                                try {
                                    String response = responseFuture.get(5, TimeUnit.SECONDS);
                                    responseFutures.remove(messageId);
                                    if (response.contains("True")) {
                                        return true;
                                    }
                                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                    logger.severe(e.getMessage());
                                }
                            }
                            return false;
                        }).collect(Collectors.toSet())
        ));
        freeHotelDtoSet.removeIf(freeHotelDto -> freeHotelDto.getRooms().isEmpty());

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
