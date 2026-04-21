package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ShiftRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Size(max = 255)
    private String description;
}
