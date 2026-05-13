package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.EmployeeDocument;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static EmployeeDocumentResponse fromEntity(EmployeeDocument document) {
        return EmployeeDocumentResponse.builder()
                .id(document.getId())
                .originalFileName(document.getOriginalFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .downloadUrl("/api/employees/documents/" + document.getId() + "/download")
                .createdAt(document.getCreatedAt())
                .build();
    }
}
