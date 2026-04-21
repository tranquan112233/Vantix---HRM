package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.ChangePasswordRequest;
import poly.edu.vantix.dto.request.UpdateProfileEmailRequest;
import poly.edu.vantix.dto.response.ProfileResponse;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(currentUserId(authentication)));
    }

    @PatchMapping("/email")
    public ResponseEntity<ProfileResponse> updateEmail(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileEmailRequest request
    ) {
        return ResponseEntity.ok(profileService.updateEmail(currentUserId(authentication), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(currentUserId(authentication), request);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }

        return principal.getId();
    }
}
