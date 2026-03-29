package com.ddhva.ielts.service.exception.error;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String message) {
        super(message);
    }
    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
