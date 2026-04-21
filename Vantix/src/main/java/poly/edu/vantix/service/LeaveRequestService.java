package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.LeaveDecisionRequest;
import poly.edu.vantix.dto.request.LeaveRequestRequest;
import poly.edu.vantix.dto.response.LeaveRequestResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.LeaveRequestRepository;
import poly.edu.vantix.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BusinessCalendarService businessCalendarService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            BusinessCalendarService businessCalendarService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.businessCalendarService = businessCalendarService;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> search(
            Long currentUserId,
            boolean viewAll,
            String scope,
            String keyword,
            LeaveRequestStatus status,
            LeaveType type,
            Integer year
    ) {
        Long employeeId = null;
        if (!viewAll || !"ALL".equalsIgnoreCase(scope)) {
            employeeId = currentEmployee(currentUserId).getId();
        }

        LocalDate rangeStart = year == null ? null : LocalDate.of(year, 1, 1);
        LocalDate rangeEnd = year == null ? null : LocalDate.of(year, 12, 31);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        return leaveRequestRepository.search(employeeId, normalizedKeyword, status, type, rangeStart, rangeEnd)
                .stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse getById(Long currentUserId, boolean viewAll, Long id) {
        LeaveRequest request = findActiveLeaveRequest(id);
        assertCanRead(currentUserId, viewAll, request);
        return LeaveRequestResponse.fromEntity(request);
    }

    @Transactional
    public LeaveRequestResponse create(Long currentUserId, boolean canCreateForOthers, LeaveRequestRequest request) {
        Employee currentEmployee = currentEmployee(currentUserId);
        Employee leaveEmployee = resolveLeaveEmployee(request.getEmployeeId(), currentEmployee, canCreateForOthers);

        LeaveRequest leaveRequest = new LeaveRequest();
        mapRequestToEntity(request, leaveRequest, leaveEmployee);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyLeaveCreated(saved, currentUserId);
        return LeaveRequestResponse.fromEntity(saved);
    }

    @Transactional
    public LeaveRequestResponse update(Long currentUserId, boolean manage, Long id, LeaveRequestRequest request) {
        LeaveRequest leaveRequest = findActiveLeaveRequest(id);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be updated");
        }

        if (!manage && !isOwnRequest(currentUserId, leaveRequest)) {
            throw new UnauthorizedException("You can only update your own leave request");
        }

        Employee currentEmployee = currentEmployee(currentUserId);
        Employee leaveEmployee = manage
                ? resolveLeaveEmployee(request.getEmployeeId(), currentEmployee, true)
                : currentEmployee;

        mapRequestToEntity(request, leaveRequest, leaveEmployee);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest.setDecidedAt(null);
        leaveRequest.setDecidedBy(null);
        leaveRequest.setDecisionNote(null);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyLeaveCreated(saved, currentUserId);
        return LeaveRequestResponse.fromEntity(saved);
    }

    @Transactional
    public LeaveRequestResponse cancel(Long currentUserId, boolean manage, Long id) {
        LeaveRequest leaveRequest = findActiveLeaveRequest(id);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be cancelled");
        }

        if (!manage && !isOwnRequest(currentUserId, leaveRequest)) {
            throw new UnauthorizedException("You can only cancel your own leave request");
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequest.setDecidedBy(findActiveUser(currentUserId));
        leaveRequest.setDecidedAt(LocalDateTime.now());
        leaveRequest.setDecisionNote("Cancelled by employee");

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyLeaveCancelled(saved, currentUserId);
        return LeaveRequestResponse.fromEntity(saved);
    }

    @Transactional
    public LeaveRequestResponse approve(Long currentUserId, Long id, LeaveDecisionRequest request) {
        return decide(currentUserId, id, LeaveRequestStatus.APPROVED, request);
    }

    @Transactional
    public LeaveRequestResponse reject(Long currentUserId, Long id, LeaveDecisionRequest request) {
        return decide(currentUserId, id, LeaveRequestStatus.REJECTED, request);
    }

    private LeaveRequestResponse decide(
            Long currentUserId,
            Long id,
            LeaveRequestStatus status,
            LeaveDecisionRequest request
    ) {
        LeaveRequest leaveRequest = findActiveLeaveRequest(id);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be decided");
        }

        if (isOwnRequest(currentUserId, leaveRequest)) {
            throw new BusinessException("You cannot approve or reject your own leave request");
        }

        leaveRequest.setStatus(status);
        leaveRequest.setDecidedBy(findActiveUser(currentUserId));
        leaveRequest.setDecidedAt(LocalDateTime.now());
        leaveRequest.setDecisionNote(request == null ? null : cleanText(request.getNote()));

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyLeaveDecision(saved, status, currentUserId);
        return LeaveRequestResponse.fromEntity(saved);
    }

    private void mapRequestToEntity(LeaveRequestRequest request, LeaveRequest leaveRequest, Employee leaveEmployee) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("endDate", "End date must be after start date");
        }
        if (leaveEmployee.getJoinDate() != null && request.getStartDate().isBefore(leaveEmployee.getJoinDate())) {
            throw new BusinessException("startDate", "Leave date must be after employee join date");
        }
        if (leaveEmployee.getTerminationDate() != null && request.getEndDate().isAfter(leaveEmployee.getTerminationDate())) {
            throw new BusinessException("endDate", "Leave date must be before employee termination date");
        }
        boolean overlap = leaveRequestRepository.existsOverlappingRequest(
                leaveEmployee.getId(),
                request.getStartDate(),
                request.getEndDate(),
                List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED),
                leaveRequest.getId()
        );
        if (overlap) {
            throw new BusinessException("startDate", "Employee already has a pending or approved leave request in this range");
        }
        BigDecimal workingLeaveDays = businessCalendarService.countWorkingLeaveDays(
                leaveEmployee.getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDayUnit() == null ? LeaveDayUnit.FULL : request.getDayUnit()
        );
        if (workingLeaveDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("startDate", "Leave request must include at least one working day");
        }
        if (request.getType() == LeaveType.ANNUAL) {
            validateAnnualLeaveBalance(leaveEmployee, leaveRequest.getId(), request, workingLeaveDays);
        }

        leaveRequest.setEmployee(leaveEmployee);
        leaveRequest.setType(request.getType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setDayUnit(request.getDayUnit() == null ? LeaveDayUnit.FULL : request.getDayUnit());
        leaveRequest.setReason(request.getReason().trim());
        leaveRequest.setEmergencyContact(cleanText(request.getEmergencyContact()));
        leaveRequest.setHandoverEmployee(resolveHandoverEmployee(request.getHandoverEmployeeId(), leaveEmployee));
    }

    private Employee resolveLeaveEmployee(Long employeeId, Employee currentEmployee, boolean canCreateForOthers) {
        if (employeeId == null) {
            return currentEmployee;
        }

        if (!canCreateForOthers && !currentEmployee.getId().equals(employeeId)) {
            throw new UnauthorizedException("You cannot create leave requests for another employee");
        }

        return findActiveEmployee(employeeId);
    }

    private void validateAnnualLeaveBalance(
            Employee employee,
            Long excludeId,
            LeaveRequestRequest request,
            BigDecimal requestedDays
    ) {
        LocalDate yearStart = LocalDate.of(request.getStartDate().getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(request.getStartDate().getYear(), 12, 31);
        BigDecimal usedOrPending = leaveRequestRepository
                .search(employee.getId(), null, null, LeaveType.ANNUAL, yearStart, yearEnd)
                .stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.PENDING || lr.getStatus() == LeaveRequestStatus.APPROVED)
                .filter(lr -> excludeId == null || !excludeId.equals(lr.getId()))
                .map(lr -> businessCalendarService.countWorkingLeaveDays(
                        employee.getId(),
                        lr.getStartDate().isBefore(yearStart) ? yearStart : lr.getStartDate(),
                        lr.getEndDate().isAfter(yearEnd) ? yearEnd : lr.getEndDate(),
                        lr.getDayUnit() == null ? LeaveDayUnit.FULL : lr.getDayUnit()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (usedOrPending.add(requestedDays).compareTo(new BigDecimal("12.0")) > 0) {
            throw new BusinessException("type", "Annual leave balance is not enough");
        }
    }

    private Employee resolveHandoverEmployee(Long handoverEmployeeId, Employee leaveEmployee) {
        if (handoverEmployeeId == null) {
            return null;
        }

        if (leaveEmployee.getId().equals(handoverEmployeeId)) {
            throw new BusinessException("handoverEmployeeId", "Handover employee must be different from leave employee");
        }

        return findActiveEmployee(handoverEmployeeId);
    }

    private void assertCanRead(Long currentUserId, boolean viewAll, LeaveRequest leaveRequest) {
        if (viewAll || isOwnRequest(currentUserId, leaveRequest)) {
            return;
        }

        throw new UnauthorizedException("You cannot view this leave request");
    }

    private boolean isOwnRequest(Long currentUserId, LeaveRequest leaveRequest) {
        return leaveRequest.getEmployee() != null
                && leaveRequest.getEmployee().getUser() != null
                && leaveRequest.getEmployee().getUser().getId().equals(currentUserId);
    }

    private Employee currentEmployee(Long currentUserId) {
        return employeeRepository.findActiveByUserId(currentUserId)
                .orElseThrow(() -> new BusinessException("Current account is not linked to an employee profile"));
    }

    private Employee findActiveEmployee(Long employeeId) {
        return employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
    }

    private User findActiveUser(Long userId) {
        return userRepository.findActiveWithRoleAndPermissionsById(userId)
                .orElseThrow(() -> new UnauthorizedException("User is no longer available"));
    }

    private LeaveRequest findActiveLeaveRequest(Long id) {
        return leaveRequestRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Leave request does not exist"));
    }

    private void notifyLeaveDecision(
            LeaveRequest leaveRequest,
            LeaveRequestStatus status,
            Long currentUserId
    ) {
        Set<Long> recipientIds = new LinkedHashSet<>();
        if (leaveRequest.getEmployee() != null && leaveRequest.getEmployee().getUser() != null) {
            recipientIds.add(leaveRequest.getEmployee().getUser().getId());
        }
        if (leaveRequest.getCreatedBy() != null) {
            recipientIds.add(leaveRequest.getCreatedBy());
        }
        recipientIds.remove(currentUserId);

        String title = status == LeaveRequestStatus.APPROVED
                ? "Đơn xin nghỉ đã được duyệt"
                : "Đơn xin nghỉ đã bị từ chối";
        String decision = status == LeaveRequestStatus.APPROVED ? "được duyệt" : "bị từ chối";
        String message = "Đơn xin nghỉ từ " + leaveRequest.getStartDate()
                + " đến " + leaveRequest.getEndDate()
                + " của bạn đã " + decision + ".";

        for (Long recipientId : recipientIds) {
            notificationService.createForUser(
                    recipientId,
                    NotificationType.LEAVE,
                    title,
                    message,
                    "/leave-requests"
            );
        }
    }

    private void notifyLeaveCreated(LeaveRequest leaveRequest, Long currentUserId) {
        Set<Long> recipientIds = approverUserIds();
        recipientIds.remove(currentUserId);

        String employeeName = employeeName(leaveRequest.getEmployee());
        String message = employeeName + " đã gửi đơn xin nghỉ từ "
                + leaveRequest.getStartDate()
                + " đến " + leaveRequest.getEndDate()
                + ".";

        notificationService.createForUsers(
                recipientIds,
                NotificationType.LEAVE,
                "Có đơn xin nghỉ mới",
                message,
                "/leave-requests"
        );
    }

    private void notifyLeaveCancelled(LeaveRequest leaveRequest, Long currentUserId) {
        Set<Long> recipientIds = approverUserIds();
        recipientIds.remove(currentUserId);

        String employeeName = employeeName(leaveRequest.getEmployee());
        String message = employeeName + " đã hủy đơn xin nghỉ từ "
                + leaveRequest.getStartDate()
                + " đến " + leaveRequest.getEndDate()
                + ".";

        notificationService.createForUsers(
                recipientIds,
                NotificationType.LEAVE,
                "Đơn xin nghỉ đã bị hủy",
                message,
                "/leave-requests"
        );
    }

    private Set<Long> approverUserIds() {
        Set<Long> userIds = new LinkedHashSet<>();
        userRepository.findActiveByAnyPermission(List.of(
                        "LEAVE_REQUEST_APPROVE",
                        "LEAVE_REQUEST_MANAGE",
                        "LEAVE_REQUEST_VIEW_ALL"
                ))
                .forEach(user -> userIds.add(user.getId()));
        return userIds;
    }

    private String employeeName(Employee employee) {
        if (employee == null || employee.getFullName() == null || employee.getFullName().isBlank()) {
            return "Nhân viên";
        }
        return employee.getFullName();
    }

    private String cleanText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
