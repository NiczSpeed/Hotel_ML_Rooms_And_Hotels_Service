package com.ml.hotel_ml_rooms_and_hotels_service.utils.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.Encryptor;
import com.ml.hotel_ml_rooms_and_hotels_service.utils.EncryptorUtil;

public class IntegerConverter extends Encryptor<Integer> {

    public IntegerConverter(EncryptorUtil encryptorUtil, ObjectMapper objectMapper) {
        super(encryptorUtil, objectMapper);
    }

    @Override
    protected Class<Integer> getTargetClass() {
        return Integer.class;
    }
}
