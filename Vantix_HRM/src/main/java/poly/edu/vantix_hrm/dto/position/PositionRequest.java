package poly.edu.vantix_hrm.dto.position;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionRequest {

    @NotBlank(message = "Position name is required")
    private String positionName;
}
