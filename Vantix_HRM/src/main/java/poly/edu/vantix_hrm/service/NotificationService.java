package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.Notification;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.NotificationRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các field 'final'
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;


    /**
     * Hàm gửi thông báo dùng chung cho toàn dự án
     */
    @Transactional
    public void sendNotification(Long userId, String title, String message, String type, String url) {
        // 1. Tạo đối tượng và lưu vào Database
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetUrl(url);

        Notification savedNote = notificationRepository.save(notification);

        // 2. Gửi Real-time qua WebSocket
        // Client sẽ lắng nghe tại: /user/{userId}/topic/notifications
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", savedNote);
        } catch (Exception e) {
            // Log lỗi WebSocket nhưng không làm roll-back giao dịch lưu DB
            System.err.println("Lỗi gửi WebSocket: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách thông báo của nhân viên
     */
    public List<Notification> getMyNotifications(Long userId) {
        // Phải gọi hàm có đuôi AndIsDeletedFalse thì load lại trang mới không thấy đồ cũ
        return notificationRepository.findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Đánh dấu đã đọc
     */
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Đếm số lượng thông báo chưa đọc (để hiện Badge trên chuông)
     */
    public long countUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void toggleStar(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setStarred(!n.isStarred()); // Đảo trạng thái sao
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void softDelete(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            // KIỂM TRA: Nếu đã đánh sao (starred) thì KHÔNG cho xóa
            if (n.isStarred()) {
                throw new IllegalStateException("Không thể xóa thông báo đã lưu!");
            }

            n.setDeleted(true); // Chỉ những cái không có sao mới chạy đến dòng này
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void deleteAllExceptStarred(Long recipientId) {
        // Tìm tất cả thông báo của User mà chưa được đánh dấu sao và chưa bị xóa
        List<Notification> normalNotes = notificationRepository
                .findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(recipientId);

        // Đánh dấu xóa cho tất cả bọn chúng
        normalNotes.forEach(n -> n.setDeleted(true));
        notificationRepository.saveAll(normalNotes);
    }

    @Transactional
    public void sendBulkSummon(String roleName, String location, String reason) {
        List<User> recipients;

        if ("ALL".equals(roleName)) {
            recipients = userRepository.findByStatusAndDeletedFalse(User.UserStatus.ACTIVE);
        } else {
            recipients = userRepository.findByRoleName(roleName);
        }

        String title = "⚡ LỆNH TRIỆU TẬP " + (roleName.equals("ALL") ? "TỔNG" : roleName);
        String message = "Vui lòng đến " + location + ". Nội dung: " + reason;

        for (User user : recipients) {
            // Tận dụng hàm sendNotification lẻ đã có của bạn
            this.sendNotification(user.getId(), title, message, "SUMMON", "/my-notifications");
        }
    }
}

