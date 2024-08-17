package com.ml.hotel_ml_rooms_and_hotels_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "numberOfStars")
    private int numberOfStars;

    @Column(name = "contact")
    private String contact;

//    @JsonManagedReference
//    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
//    private Set<Room> rooms;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
    private Set<Room> rooms;

}
