package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.DTO.LeaveRequestDTO;
import poly.edu.vantix_hrm.entity.LeaveRequest;
import poly.edu.vantix_hrm.repository.LeaveRequestRepository;
import poly.edu.vantix_hrm.service.LeaveService;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LeaveController {

    private final LeaveService service;
    private final LeaveRequestRepository repo;

    @PostMapping
    public LeaveRequest create(@RequestBody LeaveRequestDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/approve/{id}")
    public LeaveRequest approve(@PathVariable Integer id,
                                @RequestParam Integer approverId) {
        return service.approve(id, approverId);
    }

    @PutMapping("/reject/{id}")
    public LeaveRequest reject(@PathVariable Integer id,
                               @RequestParam Integer approverId) {
        return service.reject(id, approverId);
    }

    @GetMapping
    public List<LeaveRequest> getAll() {
        return repo.findAll();
    }

    @GetMapping("/employee/{id}")
    public List<LeaveRequest> getByEmployee(@PathVariable Integer id) {
        return repo.findByEmployeeEmployeeId(id);
    }
}