package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    // false khi có lỗi
    private boolean success;

    // Thông báo lỗi chính
    private String message;

    // Lỗi theo từng field, ví dụ: username -> Username already exists
    private Map<String, String> errors;

    // Thời gian lỗi
    private LocalDateTime timestamp;
}