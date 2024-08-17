package com.ml.hotel_ml_rooms_and_hotels_service.dto;

import com.ml.hotel_ml_rooms_and_hotels_service.model.RoomStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {

    private long number;
    private long numberOfBeds;
    private RoomStatus status;
    private double price;
    private String description;

}
