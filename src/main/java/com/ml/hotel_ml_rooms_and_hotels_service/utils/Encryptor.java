package com.ml.hotel_ml_rooms_and_hotels_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class Encryptor<T> implements AttributeConverter<T, String> {


    private final EncryptorUtil encryptorUtil;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(T t) {
        try {
            String json = objectMapper.writeValueAsString(t);
            return encryptorUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("A problem occurred during encryption!", e);
        }
    }

    @SneakyThrows
    @Override
    public T convertToEntityAttribute(String s) {
        try {
            String json = encryptorUtil.decrypt(s);
            return objectMapper.readValue(json, getTargetClass());
        } catch (Exception e) {
            throw new RuntimeException("A problem occurred during decryption!", e);
        }
    }

    protected Class<T> getTargetClass() {
        throw new UnsupportedOperationException("This method need to be override!");
    }

}
