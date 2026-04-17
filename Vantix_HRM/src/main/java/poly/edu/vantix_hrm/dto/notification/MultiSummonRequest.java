package poly.edu.vantix_hrm.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class MultiSummonRequest {
    private String roleName;
    private List<Long> recipientIds; // Phải trùng khớp 100% với tên biến trong Vue gửi lên
    private String location;
    private String reason;
    private String priority;
}