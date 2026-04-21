package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionRequest {

    @NotBlank(message = "Position code is required")
    @Size(max = 20, message = "Position code must be at most 20 characters")
    private String code;

    @NotBlank(message = "Position name is required")
    @Size(max = 100, message = "Position name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private Long departmentId;
}
