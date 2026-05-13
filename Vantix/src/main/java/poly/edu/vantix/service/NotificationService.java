package poly.edu.vantix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern QUOTED_TEXT = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern OVERDUE_DAYS = Pattern.compile("(\\d+)\\s+(?:ng|day)");

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
        validateContent(request.getTitle(), request.getMessage(), request.getTitleKey(), request.getMessageKey());
        List<Notification> toSave = new ArrayList<>(recipients.size());
        for (User user : recipients) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(fallbackText(request.getTitle(), request.getTitleKey()));
            notification.setMessage(fallbackText(request.getMessage(), request.getMessageKey()));
            notification.setTitleKey(blankToNull(request.getTitleKey()));
            notification.setMessageKey(blankToNull(request.getMessageKey()));
            notification.setMessageParams(serializeParams(request.getMessageParams()));
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
                    LocalizedPayload localized = inferLocalizedPayload(type, title, message, targetUrl);
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setType(type == null ? NotificationType.GENERAL : type);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    if (localized != null) {
                        notification.setTitleKey(localized.titleKey());
                        notification.setMessageKey(localized.messageKey());
                        notification.setMessageParams(serializeParams(localized.params()));
                    }
                    notification.setTargetUrl(targetUrl);
                    notification.setStatus(NotificationStatus.UNREAD);
                    Notification saved = notificationRepository.save(notification);
                    notificationWebSocketService.publish(userId, NotificationResponse.fromEntity(saved));
                });
    }

    @Transactional
    public void createLocalizedForUser(
            Long userId,
            NotificationType type,
            String titleKey,
            String messageKey,
            Map<String, String> messageParams,
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
                    notification.setTitle(fallbackText(null, titleKey));
                    notification.setMessage(fallbackText(null, messageKey));
                    notification.setTitleKey(blankToNull(titleKey));
                    notification.setMessageKey(blankToNull(messageKey));
                    notification.setMessageParams(serializeParams(messageParams));
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

    @Transactional
    public void createLocalizedForUsers(
            Collection<Long> userIds,
            NotificationType type,
            String titleKey,
            String messageKey,
            Map<String, String> messageParams,
            String targetUrl
    ) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        userIds.stream()
                .filter(id -> id != null)
                .distinct()
                .forEach(id -> createLocalizedForUser(id, type, titleKey, messageKey, messageParams, targetUrl));
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

    private void validateContent(String title, String message, String titleKey, String messageKey) {
        if (isBlank(title) && isBlank(titleKey)) {
            throw new BusinessException("Title is required");
        }
        if (isBlank(message) && isBlank(messageKey)) {
            throw new BusinessException("Message is required");
        }
    }

    private String fallbackText(String text, String key) {
        if (!isBlank(text)) {
            return text.trim();
        }
        return isBlank(key) ? "" : key.trim();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String serializeParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private LocalizedPayload inferLocalizedPayload(NotificationType type, String title, String message, String targetUrl) {
        String combined = ((title == null ? "" : title) + "\n" + (message == null ? "" : message)).toLowerCase();

        if (type == NotificationType.ATTENDANCE || "/makeup-checkouts".equals(targetUrl)) {
            String date = firstMatch(ISO_DATE, combined);
            if (combined.contains("missing checkout") || combined.contains("forgot to check out") || combined.contains("quên check-out")) {
                return new LocalizedPayload(
                        "notification.attendance.missingCheckout.title",
                        "notification.attendance.missingCheckout.message",
                        Map.of("date", date)
                );
            }
            if (combined.contains("new make-up checkout request") || combined.contains("đơn bù check-out mới")) {
                return new LocalizedPayload(
                        "notification.makeup.created.title",
                        "notification.makeup.created.message",
                        Map.of("employee", leadingName(message), "date", date)
                );
            }
            if (combined.contains("make-up checkout approved") || combined.contains("được duyệt")) {
                return new LocalizedPayload(
                        "notification.makeup.approved.title",
                        "notification.makeup.decision.message",
                        Map.of("date", date, "decision", "notification.decision.approved")
                );
            }
            if (combined.contains("make-up checkout rejected") || combined.contains("bị từ chối")) {
                return new LocalizedPayload(
                        "notification.makeup.rejected.title",
                        "notification.makeup.decision.message",
                        Map.of("date", date, "decision", "notification.decision.rejected")
                );
            }
        }

        if (type == NotificationType.LEAVE || "/leave-requests".equals(targetUrl)) {
            List<String> dates = allMatches(ISO_DATE, combined);
            Map<String, String> params = Map.of(
                    "employee", leadingName(message),
                    "startDate", dates.size() > 0 ? dates.get(0) : "",
                    "endDate", dates.size() > 1 ? dates.get(1) : "",
                    "decision", combined.contains("approved") || combined.contains("được duyệt")
                            ? "notification.decision.approved"
                            : "notification.decision.rejected"
            );
            if (combined.contains("new leave request") || combined.contains("đơn xin nghỉ mới")) {
                return new LocalizedPayload("notification.leave.created.title", "notification.leave.created.message", params);
            }
            if (combined.contains("cancelled") || combined.contains("bị hủy")) {
                return new LocalizedPayload("notification.leave.cancelled.title", "notification.leave.cancelled.message", params);
            }
            if (combined.contains("approved") || combined.contains("được duyệt")) {
                return new LocalizedPayload("notification.leave.approved.title", "notification.leave.decision.message", params);
            }
            if (combined.contains("rejected") || combined.contains("bị từ chối")) {
                return new LocalizedPayload("notification.leave.rejected.title", "notification.leave.decision.message", params);
            }
        }

        if (type == NotificationType.TASK || "/tasks".equals(targetUrl)) {
            String task = firstMatch(QUOTED_TEXT, message == null ? "" : message);
            String dueDate = firstMatch(ISO_DATE, combined);
            String days = firstMatch(OVERDUE_DAYS, combined);
            Map<String, String> params = Map.of("task", task, "dueDate", dueDate, "days", days);

            if (combined.contains("đến hạn hôm nay") || combined.contains("due today")) {
                return new LocalizedPayload("notification.task.dueToday.title", "notification.task.dueToday.message", params);
            }
            if (combined.contains("quá hạn") || combined.contains("overdue")) {
                return new LocalizedPayload("notification.task.overdue.title", "notification.task.overdue.message", params);
            }
            if (combined.contains("mới được giao") || combined.contains("assigned")) {
                return new LocalizedPayload(
                        "notification.task.assigned.title",
                        dueDate.isBlank() ? "notification.task.assigned.message" : "notification.task.assignedWithDue.message",
                        params
                );
            }
            if (combined.contains("cập nhật") || combined.contains("updated")) {
                return new LocalizedPayload("notification.task.updated.title", "notification.task.updated.message", params);
            }
            if (combined.contains("hoàn thành") || combined.contains("completed")) {
                return new LocalizedPayload("notification.task.done.title", "notification.task.done.message", params);
            }
            if (combined.contains("mở lại") || combined.contains("reopened")) {
                return new LocalizedPayload("notification.task.reopened.title", "notification.task.reopened.message", params);
            }
        }

        return null;
    }

    private String firstMatch(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value == null ? "" : value);
        if (!matcher.find()) {
            return "";
        }
        return matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group();
    }

    private List<String> allMatches(Pattern pattern, String value) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(value == null ? "" : value);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    private String leadingName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.split("\\s+(đã|has|submitted|cancelled)", 2)[0].trim();
    }

    private record LocalizedPayload(String titleKey, String messageKey, Map<String, String> params) {}

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
