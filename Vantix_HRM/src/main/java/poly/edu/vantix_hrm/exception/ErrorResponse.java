package poly.edu.vantix_hrm.exception;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    // Thời gian xảy ra lỗi
    private LocalDateTime timestamp;

    // HTTP status code (400, 401, 403, 404, 500...)
    private int status;

    // Mô tả lỗi
    private String message;

    // API path bị lỗi
    private String path;

    // Lỗi chi tiết theo từng field → FE dùng để hiện lỗi đúng ô input
    // Ví dụ: { "username": "Username đã tồn tại" }
    private Map<String, String> errors;
}