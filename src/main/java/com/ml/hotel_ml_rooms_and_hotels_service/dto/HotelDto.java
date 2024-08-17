package com.ml.hotel_ml_rooms_and_hotels_service.dto;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelDto {

    private String name;
    private String address;
    private String city;
    private String state;
    private int numberOfStars;
    private String contact;
    private Set<RoomDto> rooms;

}
