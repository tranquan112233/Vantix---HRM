package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.LeaveDecisionRequest;
import poly.edu.vantix.dto.request.LeaveRequestRequest;
import poly.edu.vantix.dto.response.LeaveRequestResponse;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.LeaveRequestService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_VIEW','LEAVE_REQUEST_CREATE','LEAVE_REQUEST_VIEW_ALL','LEAVE_REQUEST_APPROVE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<List<LeaveRequestResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "MINE") String scope,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) LeaveType type,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(leaveRequestService.search(
                currentUserId(authentication),
                canViewAll(authentication),
                scope,
                keyword,
                status,
                type,
                year
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_VIEW','LEAVE_REQUEST_VIEW_ALL','LEAVE_REQUEST_APPROVE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> getById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leaveRequestService.getById(
                currentUserId(authentication),
                canViewAll(authentication),
                id
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_CREATE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> create(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(
                currentUserId(authentication),
                canCreateForOthers(authentication),
                request
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_UPDATE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.update(
                currentUserId(authentication),
                hasAnyAuthority(authentication, "LEAVE_REQUEST_MANAGE"),
                id,
                request
        ));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_CANCEL','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> cancel(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leaveRequestService.cancel(
                currentUserId(authentication),
                hasAnyAuthority(authentication, "LEAVE_REQUEST_MANAGE"),
                id
        ));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_APPROVE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> approve(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.approve(currentUserId(authentication), id, request));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_APPROVE','LEAVE_REQUEST_MANAGE')")
    public ResponseEntity<LeaveRequestResponse> reject(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.reject(currentUserId(authentication), id, request));
    }

    private boolean canViewAll(Authentication authentication) {
        return hasAnyAuthority(
                authentication,
                "LEAVE_REQUEST_VIEW_ALL",
                "LEAVE_REQUEST_APPROVE",
                "LEAVE_REQUEST_MANAGE"
        );
    }

    private boolean canCreateForOthers(Authentication authentication) {
        return hasAnyAuthority(
                authentication,
                "LEAVE_REQUEST_APPROVE",
                "LEAVE_REQUEST_MANAGE"
        );
    }

    private boolean hasAnyAuthority(Authentication authentication, String... names) {
        Set<String> authorityNames = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (String name : names) {
            if (authorityNames.contains(name)) {
                return true;
            }
        }

        return false;
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }

        return principal.getId();
    }
}
