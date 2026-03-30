package poly.edu.vantix_hrm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
 * Exception cho lỗi nghiệp vụ
 *
 * Cách dùng:
 *   throw new BusinessException("username", "Username đã tồn tại",     HttpStatus.BAD_REQUEST);  // 400 - dữ liệu sai
 *   throw new BusinessException("userId",   "Không tìm thấy user",     HttpStatus.NOT_FOUND);    // 404 - không tìm thấy
 *   throw new BusinessException("password", "Sai tài khoản/mật khẩu", HttpStatus.UNAUTHORIZED); // 401 - chưa xác thực
 *   throw new BusinessException("role",     "Không có quyền truy cập", HttpStatus.FORBIDDEN);    // 403 - bị chặn
 */
@Getter
public class BusinessException extends RuntimeException {

    // Field bị lỗi → FE dùng để hiện lỗi đúng ô input
    private final String field;

    // HTTP status trả về
    private final HttpStatus status;

    public BusinessException(String field, String message, HttpStatus status) {
        super(message);
        this.field = field;
        this.status = status;
    }
}