package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.TaskAttachment;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long assigneeId;
    private String assigneeCode;
    private String assigneeName;
    private String departmentName;
    private String positionName;
    private LocalDate dueDate;
    private List<TaskAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(WorkTask task) {
        List<TaskAttachmentResponse> attachmentResponses = task.getAttachments() == null
                ? List.of()
                : task.getAttachments().stream()
                        .filter(attachment -> !Boolean.TRUE.equals(attachment.getDeleted()))
                        .sorted(Comparator.comparing(TaskAttachment::getId).reversed())
                        .map(TaskAttachmentResponse::fromEntity)
                        .toList();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeCode(task.getAssignee() != null ? task.getAssignee().getEmployeeCode() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .departmentName(task.getAssignee() != null && task.getAssignee().getDepartment() != null
                        ? task.getAssignee().getDepartment().getName()
                        : null)
                .positionName(task.getAssignee() != null && task.getAssignee().getPosition() != null
                        ? task.getAssignee().getPosition().getName()
                        : null)
                .dueDate(task.getDueDate())
                .attachments(attachmentResponses)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
