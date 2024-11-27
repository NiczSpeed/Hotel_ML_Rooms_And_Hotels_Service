package com.ml.hotel_ml_rooms_and_hotels_service.exceptions;

public class ErrorWhileSaveRoomException extends RuntimeException {
    public ErrorWhileSaveRoomException() {
        super("Error while saving room");
    }
}
