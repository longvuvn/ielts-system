package com.ddhva.ielts.service.exception.error;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " không tìm thấy với id: " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
