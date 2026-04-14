package poly.edu.vantix_hrm.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class MultiSummonRequest {
    private List<Long> recipientIds; // Mảng ID nhân viên đã chọn
    private String location;
    private String reason;
    private String priority; // NORMAL, MEETING, URGENT
}