package poly.edu.vantix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import poly.edu.vantix.dto.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildError(String message, Map<String, String> errors) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 1. Bắt lỗi validate từ @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // Chỉ lấy lỗi đầu tiên của mỗi field
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                buildError("Validation failed", errors)
        );
    }

    /**
     * 2. Bắt lỗi nghiệp vụ
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(
                buildError(ex.getMessage(), ex.getErrors())
        );
    }

    /**
     * 3. Bắt lỗi xác thực
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                buildError(ex.getMessage(), null)
        );
    }

    /**
     * 4. Bắt lỗi server
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerException(Exception ex) {
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildError("Internal server error", null)
        );
    }
}
