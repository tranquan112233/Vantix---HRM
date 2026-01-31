package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer notificationID;

    @Column(name = "Title", length = 150)
    private String title;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "TargetRoleID")
    private Roles targetRole;

    @ManyToOne
    @JoinColumn(name = "TargetUserID")
    private Users targetUser;

    @ManyToOne
    @JoinColumn(name = "SenderID")
    private Users sender;

    @Column(name = "AttachmentURL")
    private String attachmentURL;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
}
