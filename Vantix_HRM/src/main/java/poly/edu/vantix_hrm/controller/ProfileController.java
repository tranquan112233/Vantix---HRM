package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.DTO.UserProfileDTO;
import poly.edu.vantix_hrm.service.ProfileService;

import java.security.Principal; // NHỚ IMPORT THÊM CÁI NÀY

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    // =================================================================
    // API MỚI: Lấy Profile bằng Token (Frontend gọi cái này lúc load trang)
    // =================================================================
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(Principal principal) {
        // principal.getName() sẽ tự động lấy username lưu trong JWT Token
        String username = principal.getName();
        UserProfileDTO profile = profileService.getMyProfile(username);
        return ResponseEntity.ok(profile);
    }

    // =================================================================
    // CÁC API CŨ (Giữ lại để Update và Upload Ảnh)
    // =================================================================
    @GetMapping("/{employeeId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(profileService.getProfile(employeeId));
    }

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