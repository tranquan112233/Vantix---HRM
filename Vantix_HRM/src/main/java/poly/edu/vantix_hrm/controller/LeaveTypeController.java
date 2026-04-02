package poly.edu.vantix_hrm.controller;

import poly.edu.vantix_hrm.entity.LeaveType;
import poly.edu.vantix_hrm.service.LeaveTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin(origins = "*") // Hỗ trợ gọi từ Vue
public class LeaveTypeController {

    @Autowired
    private LeaveTypeService service;

    @PreAuthorize("hasAuthority('LEAVE_TYPE_VIEW')")
    @GetMapping
    public List<LeaveType> getAll() {
        return service.getAllLeaveTypes();
    }

    @PreAuthorize("hasAuthority('LEAVE_TYPE_CREATE')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody LeaveType leaveType) {
        try {
            return ResponseEntity.ok(service.createLeaveType(leaveType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('LEAVE_TYPE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LeaveType leaveType) {
        try {
            return ResponseEntity.ok(service.updateLeaveType(id, leaveType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('LEAVE_TYPE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteLeaveType(id);
        return ResponseEntity.ok().build();
    }
}