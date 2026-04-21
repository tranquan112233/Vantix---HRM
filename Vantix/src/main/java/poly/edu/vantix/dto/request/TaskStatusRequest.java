package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.TaskStatus;

@Getter
@Setter
public class TaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
