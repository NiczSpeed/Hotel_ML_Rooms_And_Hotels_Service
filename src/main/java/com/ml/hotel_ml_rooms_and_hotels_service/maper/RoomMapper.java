package com.ml.hotel_ml_rooms_and_hotels_service.maper;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.RoomDto;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Room;
import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RoomMapper {


    RoomMapper Instance = Mappers.getMapper(RoomMapper.class);

    Room mapRoomDtoToRoom(RoomDto roomDto);
}
