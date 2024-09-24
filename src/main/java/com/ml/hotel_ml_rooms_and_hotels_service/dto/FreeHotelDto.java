package com.ml.hotel_ml_rooms_and_hotels_service.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeHotelDto {

    private String name;
    private int numberOfStars;
    private String contact;
    private Set<RoomDto> rooms;

}
