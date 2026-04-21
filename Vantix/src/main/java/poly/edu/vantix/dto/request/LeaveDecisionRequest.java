package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveDecisionRequest {

    @Size(max = 4000, message = "Decision note must be at most 4000 characters")
    private String note;
}
