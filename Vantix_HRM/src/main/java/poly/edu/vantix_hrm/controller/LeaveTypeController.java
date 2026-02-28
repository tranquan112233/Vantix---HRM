package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.LeaveType;
import poly.edu.vantix_hrm.repository.LeaveTypeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LeaveTypeController {

    private final LeaveTypeRepository repo;

    @GetMapping
    public List<LeaveType> getAll() {
        return repo.findAll();
    }
}