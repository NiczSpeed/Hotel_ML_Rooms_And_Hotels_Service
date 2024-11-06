package com.ml.hotel_ml_rooms_and_hotels_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.converters.IntegerConverter;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.converters.StringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "name")
    @Convert(converter = StringConverter.class)
    private String name;

    @Column(name = "address")
    @Convert(converter = StringConverter.class)
    private String address;

    @Column(name = "city")
    @Convert(converter = StringConverter.class)
    private String city;

    @Column(name = "state")
    @Convert(converter = StringConverter.class)
    private String state;

    @Column(name = "numberOfStars")
    @Convert(converter = IntegerConverter.class)
    private Integer numberOfStars;

    @Column(name = "contact")
    @Convert(converter = StringConverter.class)
    private String contact;

//    @JsonManagedReference
//    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
//    private Set<Room> rooms;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
    private Set<Room> rooms;

}
