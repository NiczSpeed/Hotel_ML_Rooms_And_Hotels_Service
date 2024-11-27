package com.ml.hotel_ml_rooms_and_hotels_service.dto;

import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import lombok.*;

@Data
@Builder
public class RoomDto {

    private Long number;
    private Long numberOfBeds;
    private RoomStatus status;
    private Double weekPrice;
    private Double weekendPrice;
    private String description;

}
