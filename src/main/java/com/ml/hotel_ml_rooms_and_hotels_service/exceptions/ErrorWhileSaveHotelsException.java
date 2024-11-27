package com.ml.hotel_ml_rooms_and_hotels_service.exceptions;

public class ErrorWhileSaveHotelsException extends RuntimeException {
    public ErrorWhileSaveHotelsException() {
        super("Error while saving hotel");
    }
}
