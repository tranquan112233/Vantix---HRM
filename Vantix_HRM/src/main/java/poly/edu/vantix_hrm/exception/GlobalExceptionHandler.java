package poly.edu.vantix_hrm.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // Validation Error
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    errors.putIfAbsent(
                            error.getField(),
                            error.getDefaultMessage()
                    );
                });

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Validation Error")
                .message("Dữ liệu không hợp lệ")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();
    }


    // =========================
    // Business Error
    // =========================
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Business Error")
                .message("Business validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();
    }


    // =========================
    // All other errors
    // =========================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(
            Exception ex,
            HttpServletRequest request
    ) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

}