package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.TaskAttachment;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static TaskAttachmentResponse fromEntity(TaskAttachment attachment) {
        return TaskAttachmentResponse.builder()
                .id(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .downloadUrl("/api/tasks/attachments/" + attachment.getId() + "/download")
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
