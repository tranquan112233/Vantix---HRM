package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.DTO.ProfileDTO;
import poly.edu.vantix_hrm.DTO.ProfileUpdateDTO;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.service.ProfileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileDTO getMyProfile() {
        return profileService.getMyProfile();
    }

    @PutMapping
    public String updateProfile(@RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return "Profile updated successfully";
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        User user = (User) authentication.getPrincipal();

        String uploadDir = "uploads/avatars/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = user.getId() + ".jpg";

        Path path = Paths.get(uploadDir + filename);
        file.transferTo(path.toFile());

        return ResponseEntity.ok("Upload success");
    }
}