package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.NotificationRequest;
import poly.edu.vantix.dto.response.NotificationResponse;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.entity.Notification;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.NotificationStatus;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.NotificationRepository;
import poly.edu.vantix.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketService notificationWebSocketService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationWebSocketService notificationWebSocketService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationWebSocketService = notificationWebSocketService;
    }

    public List<NotificationResponse> getByUserId(Long userId) {
        return getByUserId(userId, null, (NotificationType) null);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUserId(Long userId, NotificationStatus status) {
        return getByUserId(userId, status, (NotificationType) null);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUserId(Long userId, NotificationStatus status, NotificationType type) {
        return notificationRepository.search(userId, status, type).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getByUserId(
            Long userId,
            NotificationStatus status,
            Pageable pageable
    ) {
        return getByUserId(userId, status, null, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getByUserId(
            Long userId,
            NotificationStatus status,
            NotificationType type,
            Pageable pageable
    ) {
        return PageResponse.from(
                notificationRepository.search(userId, status, type, pageable),
                NotificationResponse::fromEntity
        );
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndStatusAndDeletedFalse(userId, NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        List<NotificationResponse> created = createBulk(request);
        return created.isEmpty() ? null : created.get(0);
    }

    @Transactional
    public List<NotificationResponse> createBulk(NotificationRequest request) {
        List<User> recipients = resolveRecipients(request);
        if (recipients.isEmpty()) {
            throw new BusinessException("Please select at least one recipient");
        }

        NotificationType type = resolveType(request.getType());
        List<Notification> toSave = new ArrayList<>(recipients.size());
        for (User user : recipients) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setTargetUrl(request.getTargetUrl());
            notification.setStatus(NotificationStatus.UNREAD);
            notification.setType(type);
            toSave.add(notification);
        }

        List<NotificationResponse> created = notificationRepository.saveAll(toSave).stream()
                .map(NotificationResponse::fromEntity)
                .toList();

        created.forEach(notification -> notificationWebSocketService.publish(notification.getUserId(), notification));
        return created;
    }

    @Transactional
    public void createForUser(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String targetUrl
    ) {
        if (userId == null) {
            return;
        }

        userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .ifPresent(user -> {
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setType(type == null ? NotificationType.GENERAL : type);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setTargetUrl(targetUrl);
                    notification.setStatus(NotificationStatus.UNREAD);
                    Notification saved = notificationRepository.save(notification);
                    notificationWebSocketService.publish(userId, NotificationResponse.fromEntity(saved));
                });
    }

    @Transactional
    public void createForUsers(
            Collection<Long> userIds,
            NotificationType type,
            String title,
            String message,
            String targetUrl
    ) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        userIds.stream()
                .filter(id -> id != null)
                .distinct()
                .forEach(id -> createForUser(id, type, title, message, targetUrl));
    }

    private List<User> resolveRecipients(NotificationRequest request) {
        if (Boolean.TRUE.equals(request.getSendToAll())) {
            return userRepository.search(null);
        }

        Set<Long> ids = new LinkedHashSet<>();
        if (request.getUserIds() != null) {
            for (Long id : request.getUserIds()) {
                if (id != null) ids.add(id);
            }
        }
        if (request.getUserId() != null) {
            ids.add(request.getUserId());
        }

        if (ids.isEmpty()) {
            throw new BusinessException("Please select at least one recipient");
        }

        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new BusinessException("One or more recipients do not exist");
        }
        return users;
    }

    private NotificationType resolveType(String raw) {
        if (raw == null) return NotificationType.GENERAL;
        try {
            return NotificationType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return NotificationType.GENERAL;
        }
    }

    @Transactional
    public NotificationResponse markAsRead(Long userId, Long id) {
        Notification notification = findOwnedNotification(userId, id);
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return NotificationResponse.fromEntity(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Notification notification = findOwnedNotification(userId, id);
        notification.setDeleted(true);
        notification.setDeletedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private Notification findOwnedNotification(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notification does not exist"));

        Long ownerId = notification.getUser() == null ? null : notification.getUser().getId();
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new UnauthorizedException("You cannot access this notification");
        }

        return notification;
    }
}
