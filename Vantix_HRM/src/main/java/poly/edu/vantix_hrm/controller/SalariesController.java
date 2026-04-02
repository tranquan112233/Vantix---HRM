package poly.edu.vantix_hrm.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix_hrm.service.SalariesService;

@RestController
@RequestMapping("api/salaries")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SalariesController {

    private final SalariesService salariesService;

    @GetMapping("/departments")
    public ResponseEntity<?> getDepartmentNames() {
        try {
            return ResponseEntity.ok(salariesService.findDepartmentNames());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

}
