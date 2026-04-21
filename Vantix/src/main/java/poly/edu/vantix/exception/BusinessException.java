package poly.edu.vantix.exception;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    // Dùng cho trường hợp lỗi theo field
    private final Map<String, String> errors;

    // Chỉ có message
    public BusinessException(String message) {
        super(message);
        this.errors = null;
    }

    // Có field + message
    public BusinessException(String field, String message) {
        super(message);
        this.errors = new LinkedHashMap<>();
        this.errors.put(field, message);
    }

    // Có nhiều field lỗi
    public BusinessException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}