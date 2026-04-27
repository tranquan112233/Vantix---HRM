package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MakeupCheckoutCreateRequest {

    @NotNull(message = "Attendance id is required")
    private Long attendanceId;

    @NotNull(message = "Requested check-out time is required")
    private LocalDateTime requestedCheckOutAt;

    @NotBlank(message = "Reason is required")
    @Size(max = 4000, message = "Reason must be at most 4000 characters")
    private String reason;
}
