package com.ml.hotel_ml_rooms_and_hotels_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "number")
    private long number;

    @Column(name = "numberOfBeds")
    private long numberOfBeds;

    @Column(name = "status")
    private RoomStatus status;

    @Column(name = "price")
    private double price;

    @Column(name = "description")
    private String description;

//    @JsonBackReference
//    @ManyToOne
//    private Hotel hotel;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_uuid", referencedColumnName = "uuid")
    private Hotel hotel;

}
