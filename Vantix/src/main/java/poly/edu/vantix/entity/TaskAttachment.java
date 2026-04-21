package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task_attachments")
public class TaskAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private WorkTask task;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;
}
