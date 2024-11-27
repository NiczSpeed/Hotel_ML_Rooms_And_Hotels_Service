package com.ml.hotel_ml_rooms_and_hotels_service.maper;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelMapper Instance = Mappers.getMapper(HotelMapper.class);

    Hotel mapHotelDtoToHotel(HotelDto hotelDto);

    HotelDto mapHotelToHotelDto(Hotel hotel);

    List<HotelDto> mapHotelListToHotelDtoList(List<Hotel> hotelList);
}
