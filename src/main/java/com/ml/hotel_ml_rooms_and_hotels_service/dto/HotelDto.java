package com.ml.hotel_ml_rooms_and_hotels_service.dto;

import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelDto {

    private String name;
    private String address;
    private String city;
    private String state;
    private Integer numberOfStars;
    private String contact;
    private Set<RoomDto> rooms;

}
