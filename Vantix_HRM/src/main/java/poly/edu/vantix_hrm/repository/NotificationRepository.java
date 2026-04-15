package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy danh sách thông báo của 1 nhân viên, xếp cái mới nhất lên đầu
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Đếm số thông báo chưa đọc để hiện số trên cái chuông ở Vue.js
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    // Chỉ lấy những cái chưa bị xóa (isDeleted = false)
    // Đổi tên hàm để Spring Data JPA tự thêm điều kiện WHERE is_deleted = false
    List<Notification> findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long recipientId);
}
