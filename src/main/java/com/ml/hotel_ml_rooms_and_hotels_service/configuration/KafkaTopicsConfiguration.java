package com.ml.hotel_ml_rooms_and_hotels_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

    @Bean
    public NewTopic createHotelTopic(){
        return TopicBuilder.name("create_hotel_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic createRoomTopic(){
        return TopicBuilder.name("create_room_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic requestAllHotelsTopic(){
        return TopicBuilder.name("request_all_hotels_by_city_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic responseAllHotelsTopic(){
        return TopicBuilder.name("response_all_hotels_by_city_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic requestAllHotelsCitiesTopic(){
        return TopicBuilder.name("request_all_hotels_cities_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic responseAllHotelsCitiesTopic(){
        return TopicBuilder.name("response_all_hotels_cities_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic requestFreeHotelsTopic(){
        return TopicBuilder.name("request_free_hotels_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic responseFreeHotelsTopic(){
        return TopicBuilder.name("response_free_hotels_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic checkRoomReservationPriceTopic(){
        return TopicBuilder.name("check_room_reservation_price_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic roomPriceTopic(){
        return TopicBuilder.name("room_price_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }



}
