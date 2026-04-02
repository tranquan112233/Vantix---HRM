package poly.edu.vantix_hrm.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Bắt và xử lý lỗi toàn hệ thống
 *
 * Có 3 loại lỗi:
 *   1. Lỗi @Valid        → 400 BAD_REQUEST  (lỗi từng field hiện lên ô input)
 *   2. Lỗi nghiệp vụ    → status tuỳ throw (dùng BusinessException)
 *   3. Lỗi hệ thống     → 500 INTERNAL_SERVER_ERROR
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── 1. Lỗi @Valid → 400 ─────────────────────────────────────────────────
    // Tự động bắt khi Controller dùng @Valid
    // Trả về errors với từng field để FE hiện đúng ô input

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu đầu vào không hợp lệ")
                .path(request.getRequestURI())
                .errors(errors)
                .build();
    }

    // ─── 2. Lỗi nghiệp vụ → status tuỳ theo lúc throw ───────────────────────
    // Bắt khi throw new BusinessException(...)
    // Dùng ResponseEntity để status linh hoạt (400, 401, 403, 404...)

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                                                        HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(Map.of(ex.getField(), ex.getMessage()))
                .build();

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // ─── 3. Lỗi không có quyền → 403 ────────────────────────────────────────
    // Bắt khi @PreAuthorize thất bại (user thiếu permission)

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .message("Bạn không có quyền thực hiện thao tác này")
                .path(request.getRequestURI())
                .build();
    }

    // ─── 4. Lỗi hệ thống → 500 ───────────────────────────────────────────────
    // Bắt tất cả Exception không xử lý ở trên
    // Log đầy đủ để debug, không lộ chi tiết lỗi ra ngoài

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xử lý được tại [{}]: ", request.getRequestURI(), ex);

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Hệ thống đang gặp sự cố, vui lòng thử lại sau")
                .path(request.getRequestURI())
                .build();
    }
}