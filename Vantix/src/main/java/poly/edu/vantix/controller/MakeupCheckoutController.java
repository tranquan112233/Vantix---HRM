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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.MakeupCheckoutCreateRequest;
import poly.edu.vantix.dto.request.MakeupCheckoutDecisionRequest;
import poly.edu.vantix.dto.response.MakeupCheckoutResponse;
import poly.edu.vantix.entity.enums.MakeupCheckoutStatus;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.MakeupCheckoutService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/makeup-checkouts")
public class MakeupCheckoutController {

    private final MakeupCheckoutService service;

    public MakeupCheckoutController(MakeupCheckoutService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_VIEW','MAKEUP_CHECKOUT_CREATE','MAKEUP_CHECKOUT_APPROVE','MAKEUP_CHECKOUT_MANAGE','ATTENDANCE_VIEW_ALL')")
    public ResponseEntity<List<MakeupCheckoutResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "MINE") String scope,
            @RequestParam(required = false) MakeupCheckoutStatus status
    ) {
        return ResponseEntity.ok(service.search(
                currentUserId(authentication),
                canViewAll(authentication),
                scope,
                status
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_VIEW','MAKEUP_CHECKOUT_APPROVE','MAKEUP_CHECKOUT_MANAGE','ATTENDANCE_VIEW_ALL')")
    public ResponseEntity<MakeupCheckoutResponse> getById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getById(
                currentUserId(authentication),
                canViewAll(authentication),
                id
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_CREATE','MAKEUP_CHECKOUT_MANAGE')")
    public ResponseEntity<MakeupCheckoutResponse> create(
            Authentication authentication,
            @Valid @RequestBody MakeupCheckoutCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.create(currentUserId(authentication), request)
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_CREATE','MAKEUP_CHECKOUT_MANAGE')")
    public ResponseEntity<MakeupCheckoutResponse> cancel(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.cancel(currentUserId(authentication), id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_APPROVE','MAKEUP_CHECKOUT_MANAGE')")
    public ResponseEntity<MakeupCheckoutResponse> approve(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) MakeupCheckoutDecisionRequest decision
    ) {
        return ResponseEntity.ok(service.approve(currentUserId(authentication), id, decision));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('MAKEUP_CHECKOUT_APPROVE','MAKEUP_CHECKOUT_MANAGE')")
    public ResponseEntity<MakeupCheckoutResponse> reject(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) MakeupCheckoutDecisionRequest decision
    ) {
        return ResponseEntity.ok(service.reject(currentUserId(authentication), id, decision));
    }

    private boolean canViewAll(Authentication authentication) {
        return hasAnyAuthority(
                authentication,
                "MAKEUP_CHECKOUT_APPROVE",
                "MAKEUP_CHECKOUT_MANAGE",
                "ATTENDANCE_VIEW_ALL"
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
