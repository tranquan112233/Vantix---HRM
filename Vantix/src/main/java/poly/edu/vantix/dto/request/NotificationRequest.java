package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NotificationRequest {

    // Giu lai de tuong thich nguoc (gui cho 1 user)
    private Long userId;

    // Gui cho nhieu user cung luc
    private List<Long> userIds;

    // Neu true se gui cho toan bo user dang hoat dong
    private Boolean sendToAll;

    private String type;

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String targetUrl;
}
