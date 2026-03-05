package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.DTO.ProfileDTO;
import poly.edu.vantix_hrm.service.ProfileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin("*")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // Báo lỗi 401 nếu chưa đăng nhập
        }

        String currentUsername = principal.getName();
        ProfileDTO profile = profileService.getProfileByUsername(currentUsername);

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        try {
            String username = principal.getName();
            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 1. Tìm và xóa ảnh cũ của user này (để tránh rác nếu họ đổi từ .jpg sang .png)
            File[] existingFiles = dir.listFiles((d, name) -> name.startsWith(username + "."));
            if (existingFiles != null) {
                for (File f : existingFiles) {
                    f.delete();
                }
            }

            // 2. Lấy đuôi file mới (.jpg, .png, ...)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. Lưu file với tên: username.extension (VD: admin.jpg)
            String newFilename = username + extension;
            Path filePath = Paths.get(uploadDir + newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/avatars/" + newFilename;
            return ResponseEntity.ok().body("{\"message\": \"Upload thành công\", \"url\": \"" + fileUrl + "\"}");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}