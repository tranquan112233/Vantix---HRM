package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NotificationRequest {

    // Giu lai de tuong thich nguoc (gui cho 1 user)
    private Long userId;

    // Gui cho nhieu user cung luc
    private List<Long> userIds;

    // Neu true se gui cho toan bo user dang hoat dong
    private Boolean sendToAll;

    private String type;

    @Size(max = 150)
    private String title;

    private String message;

    @Size(max = 150)
    private String titleKey;

    @Size(max = 150)
    private String messageKey;

    private Map<String, String> messageParams;

    private String targetUrl;
}
