package poly.edu.vantix.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class WorkTask extends BaseEntity {

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_employee_id")
    private Employee assignee;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "last_overdue_notified_at")
    private LocalDate lastOverdueNotifiedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<TaskAttachment> attachments = new ArrayList<>();
}
