package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.employee.EmployeeRequest;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponse;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Integer id) {
        return employeeService.getById(id);
    }

    @PostMapping
    public EmployeeResponse create(@RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Integer id,
                                   @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        employeeService.delete(id);
    }
}