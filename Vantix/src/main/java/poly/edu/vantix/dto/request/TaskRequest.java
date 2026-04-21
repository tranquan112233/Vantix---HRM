package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.TaskStatus;

import java.time.LocalDate;

@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 160, message = "Title must be at most 160 characters")
    private String title;

    @Size(max = 4000, message = "Description must be at most 4000 characters")
    private String description;

    private TaskStatus status;

    private Long assigneeId;

    private LocalDate dueDate;
}
