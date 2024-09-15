package com.ml.hotel_ml_rooms_and_hotels_service.maper;

import com.ml.hotel_ml_rooms_and_hotels_service.dto.FreeHotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.dto.HotelDto;
import com.ml.hotel_ml_rooms_and_hotels_service.model.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelMapper Instance = Mappers.getMapper(HotelMapper.class);


    Hotel mapHotelDtoToHotel(HotelDto hotelDto);

    HotelDto mapHotelToHotelDto(Hotel hotel);


    List<Hotel> mapHotelListToHotelList(List<HotelDto> hotelDtoList);
    List<HotelDto> mapHotelListToHotelDtoList(List<Hotel> hotelList);

    Set<Hotel> mapHotelDtoSetToHotelSet(Set<HotelDto> hotelDtoSet);
    Set<HotelDto> mapHotelSetToHotelDtoSet(Set<Hotel> hotelSet);



}
