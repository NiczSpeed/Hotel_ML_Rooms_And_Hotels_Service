package com.ml.hotel_ml_rooms_and_hotels_service.model;

import com.ml.hotel_ml_rooms_and_hotels_service.utils.converters.DoubleConverter;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.converters.LongConverter;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.converters.StringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "hotel")
@Table(name = "Rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "number")
    private Long number;

    @Column(name = "numberOfBeds")
    @Convert(converter = LongConverter.class)
    private Long numberOfBeds;

    @Column(name = "status")
    @Convert(converter = StringConverter.class)
    private RoomStatus status;

    @Column(name = "weekPrice")
    @Convert(converter = DoubleConverter.class)
    private Double weekPrice;

    @Column(name = "weekendPrice")
    @Convert(converter = DoubleConverter.class)
    private Double weekendPrice;

    @Column(name = "description")
    @Convert(converter = StringConverter.class)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_uuid", referencedColumnName = "uuid")
    private Hotel hotel;

}
