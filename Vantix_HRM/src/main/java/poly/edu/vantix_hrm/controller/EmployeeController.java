package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.DTO.EmployeeRequest;
import poly.edu.vantix_hrm.DTO.EmployeeResponse;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.findAll();
    }

    @PostMapping
    public EmployeeResponse create(
            @Valid @RequestBody EmployeeRequest request
    ) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        employeeService.delete(id);
    }
}

