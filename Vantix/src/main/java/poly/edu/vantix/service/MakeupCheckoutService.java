package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.MakeupCheckoutCreateRequest;
import poly.edu.vantix.dto.request.MakeupCheckoutDecisionRequest;
import poly.edu.vantix.dto.response.MakeupCheckoutResponse;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.MakeupCheckoutRequest;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.entity.enums.MakeupCheckoutStatus;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.AttendanceRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.MakeupCheckoutRequestRepository;
import poly.edu.vantix.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MakeupCheckoutService {

    private final MakeupCheckoutRequestRepository repository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AttendanceService attendanceService;

    public MakeupCheckoutService(
            MakeupCheckoutRequestRepository repository,
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AttendanceService attendanceService
    ) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.attendanceService = attendanceService;
    }

    @Transactional(readOnly = true)
    public List<MakeupCheckoutResponse> search(
            Long currentUserId,
            boolean viewAll,
            String scope,
            MakeupCheckoutStatus status
    ) {
        Long employeeId;
        if (!viewAll || !"ALL".equalsIgnoreCase(scope)) {
            employeeId = currentEmployee(currentUserId).getId();
        } else {
            employeeId = null;
        }
        return repository.search(employeeId, status).stream()
                .map(MakeupCheckoutResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MakeupCheckoutResponse getById(Long currentUserId, boolean viewAll, Long id) {
        MakeupCheckoutRequest request = findActive(id);
        if (!viewAll && !isOwner(currentUserId, request)) {
            throw new UnauthorizedException("You cannot view this make-up checkout request");
        }
        return MakeupCheckoutResponse.fromEntity(request);
    }

    @Transactional
    public MakeupCheckoutResponse create(Long currentUserId, MakeupCheckoutCreateRequest payload) {
        Employee employee = currentEmployee(currentUserId);
        Attendance attendance = attendanceRepository.findById(payload.getAttendanceId())
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .orElseThrow(() -> new BusinessException("attendanceId", "Attendance does not exist"));

        if (attendance.getEmployee() == null || !attendance.getEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You can only request a make-up checkout for your own attendance");
        }
        if (attendance.getCheckInAt() == null) {
            throw new BusinessException("attendanceId", "Cannot request make-up checkout without a check-in");
        }
        if (attendance.getCheckOutAt() != null) {
            throw new BusinessException("attendanceId", "This attendance already has a check-out time");
        }

        validateRequestedTime(attendance, payload.getRequestedCheckOutAt());

        if (repository.countPendingForAttendance(attendance.getId(), null) > 0) {
            throw new BusinessException("attendanceId", "A pending make-up checkout request already exists for this attendance");
        }

        MakeupCheckoutRequest entity = new MakeupCheckoutRequest();
        entity.setEmployee(employee);
        entity.setAttendance(attendance);
        entity.setRequestedCheckOutAt(payload.getRequestedCheckOutAt());
        entity.setReason(payload.getReason().trim());
        entity.setStatus(MakeupCheckoutStatus.PENDING);

        MakeupCheckoutRequest saved = repository.save(entity);
        notifyCreated(saved, currentUserId);
        return MakeupCheckoutResponse.fromEntity(saved);
    }

    @Transactional
    public MakeupCheckoutResponse cancel(Long currentUserId, Long id) {
        MakeupCheckoutRequest request = findActive(id);
        if (!isOwner(currentUserId, request)) {
            throw new UnauthorizedException("You can only cancel your own request");
        }
        if (request.getStatus() != MakeupCheckoutStatus.PENDING) {
            throw new BusinessException("Only pending requests can be cancelled");
        }

        request.setStatus(MakeupCheckoutStatus.CANCELLED);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(findActiveUser(currentUserId));
        request.setDecisionNote("Đã hủy bởi nhân viên / Cancelled by employee");
        return MakeupCheckoutResponse.fromEntity(repository.save(request));
    }

    @Transactional
    public MakeupCheckoutResponse approve(Long currentUserId, Long id, MakeupCheckoutDecisionRequest decision) {
        return decide(currentUserId, id, MakeupCheckoutStatus.APPROVED, decision);
    }

    @Transactional
    public MakeupCheckoutResponse reject(Long currentUserId, Long id, MakeupCheckoutDecisionRequest decision) {
        return decide(currentUserId, id, MakeupCheckoutStatus.REJECTED, decision);
    }

    private MakeupCheckoutResponse decide(
            Long currentUserId,
            Long id,
            MakeupCheckoutStatus status,
            MakeupCheckoutDecisionRequest decision
    ) {
        MakeupCheckoutRequest request = findActive(id);
        if (request.getStatus() != MakeupCheckoutStatus.PENDING) {
            throw new BusinessException("Only pending requests can be decided");
        }
        if (isOwner(currentUserId, request)) {
            throw new BusinessException("You cannot approve or reject your own request");
        }

        request.setStatus(status);
        request.setDecidedBy(findActiveUser(currentUserId));
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionNote(decision == null ? null : cleanText(decision.getNote()));

        if (status == MakeupCheckoutStatus.APPROVED) {
            applyToAttendance(request);
        }

        MakeupCheckoutRequest saved = repository.save(request);
        notifyDecision(saved, status, currentUserId);
        return MakeupCheckoutResponse.fromEntity(saved);
    }

    private void applyToAttendance(MakeupCheckoutRequest request) {
        Attendance attendance = request.getAttendance();
        if (attendance.getCheckOutAt() != null) {
            throw new BusinessException("Attendance was already closed by another action");
        }
        attendance.setCheckOutAt(request.getRequestedCheckOutAt());
        AttendanceStatus finalStatus = attendanceService.computeFinalStatus(attendance, attendance.getSchedule());
        attendance.setStatus(finalStatus);
        String makeupNote = "Bù check-out theo đơn / Make-up checkout from request #" + request.getId();
        String existingNote = attendance.getNote();
        attendance.setNote(existingNote == null || existingNote.isBlank()
                ? makeupNote
                : existingNote + " | " + makeupNote);
        attendanceRepository.save(attendance);
    }

    private void validateRequestedTime(Attendance attendance, LocalDateTime requestedCheckOutAt) {
        if (attendance.getCheckInAt() != null && !requestedCheckOutAt.isAfter(attendance.getCheckInAt())) {
            throw new BusinessException("requestedCheckOutAt", "Requested check-out time must be after check-in");
        }
        if (requestedCheckOutAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException("requestedCheckOutAt", "Requested check-out time cannot be in the future");
        }
    }

    private MakeupCheckoutRequest findActive(Long id) {
        return repository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Make-up checkout request does not exist"));
    }

    private boolean isOwner(Long currentUserId, MakeupCheckoutRequest request) {
        return request.getEmployee() != null
                && request.getEmployee().getUser() != null
                && request.getEmployee().getUser().getId().equals(currentUserId);
    }

    private Employee currentEmployee(Long currentUserId) {
        return employeeRepository.findActiveByUserId(currentUserId)
                .orElseThrow(() -> new BusinessException("Current account is not linked to an employee profile"));
    }

    private User findActiveUser(Long userId) {
        return userRepository.findActiveWithRoleAndPermissionsById(userId)
                .orElseThrow(() -> new UnauthorizedException("User is no longer available"));
    }

    private void notifyCreated(MakeupCheckoutRequest request, Long currentUserId) {
        Set<Long> approvers = approverUserIds();
        approvers.remove(currentUserId);
        String employeeName = employeeName(request);
        String workDate = String.valueOf(request.getAttendance().getWorkDate());
        String message = employeeName + " đã gửi đơn bù check-out cho ngày " + workDate + "."
                + "\n" + employeeName + " has submitted a make-up checkout request for " + workDate + ".";

        notificationService.createForUsers(
                approvers,
                NotificationType.ATTENDANCE,
                "Có đơn bù check-out mới / New make-up checkout request",
                message,
                "/makeup-checkouts"
        );
    }

    private void notifyDecision(MakeupCheckoutRequest request, MakeupCheckoutStatus status, Long currentUserId) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (request.getEmployee() != null && request.getEmployee().getUser() != null) {
            recipients.add(request.getEmployee().getUser().getId());
        }
        if (request.getCreatedBy() != null) {
            recipients.add(request.getCreatedBy());
        }
        recipients.remove(currentUserId);

        String title = status == MakeupCheckoutStatus.APPROVED
                ? "Đơn bù check-out đã được duyệt / Make-up checkout approved"
                : "Đơn bù check-out đã bị từ chối / Make-up checkout rejected";
        String decisionVi = status == MakeupCheckoutStatus.APPROVED ? "được duyệt" : "bị từ chối";
        String decisionEn = status == MakeupCheckoutStatus.APPROVED ? "approved" : "rejected";
        String workDate = String.valueOf(request.getAttendance().getWorkDate());
        String message = "Đơn bù check-out cho ngày " + workDate + " của bạn đã " + decisionVi + "."
                + "\nYour make-up checkout request for " + workDate + " has been " + decisionEn + ".";

        for (Long recipientId : recipients) {
            notificationService.createForUser(
                    recipientId,
                    NotificationType.ATTENDANCE,
                    title,
                    message,
                    "/makeup-checkouts"
            );
        }
    }

    private Set<Long> approverUserIds() {
        Set<Long> userIds = new LinkedHashSet<>();
        userRepository.findActiveByAnyPermission(List.of(
                        "MAKEUP_CHECKOUT_APPROVE",
                        "MAKEUP_CHECKOUT_MANAGE",
                        "ATTENDANCE_VIEW_ALL"
                ))
                .forEach(user -> userIds.add(user.getId()));
        return userIds;
    }

    private String employeeName(MakeupCheckoutRequest request) {
        if (request.getEmployee() == null
                || request.getEmployee().getFullName() == null
                || request.getEmployee().getFullName().isBlank()) {
            return "Nhân viên";
        }
        return request.getEmployee().getFullName();
    }

    private String cleanText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
