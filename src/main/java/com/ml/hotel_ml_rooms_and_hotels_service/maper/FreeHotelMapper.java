package com.ml.hotel_ml_rooms_and_hotels_service.maper;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.FreeHotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface FreeHotelMapper {

    FreeHotelMapper Instance = Mappers.getMapper(FreeHotelMapper.class);


    Hotel mapFreeHotelDtoToHotel(FreeHotelDto freeHotelDto);

    FreeHotelDto mapHotelToFreeHotelDto(Hotel hotel);


    Set<Hotel> mapFreeHotelDtoSetToHotelSet(Set<HotelDto> hotelDtoSet);
    Set<FreeHotelDto> mapHotelSetToFreeHotelDtoSet(Set<Hotel> hotelSet);

}
