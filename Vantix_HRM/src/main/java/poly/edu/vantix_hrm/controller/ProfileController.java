package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.DTO.UserProfileDTO;
import poly.edu.vantix_hrm.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(profileService.getProfile(employeeId));
    }

    // SỬA Ở ĐÂY: Đổi đường dẫn và gọi method updateProfile
    @PutMapping("/{employeeId}")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable Integer employeeId,
            @RequestBody UserProfileDTO dto) {
        return ResponseEntity.ok(profileService.updateProfile(employeeId, dto));
    }

    @PostMapping("/{employeeId}/avatar")
    public ResponseEntity<String> uploadAvatar(
            @PathVariable Integer employeeId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(employeeId, file);
        return ResponseEntity.ok(avatarUrl);
    }
}