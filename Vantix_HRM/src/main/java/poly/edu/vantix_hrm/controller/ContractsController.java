package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.service.ContractsService;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("api/contracts")
@CrossOrigin("*")
public class ContractsController {

    @Autowired
    private ContractsService contractsService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/getAllContracts")
    public ResponseEntity<List<Contract>> getAllContracts() {
        List<Contract> result = contractsService.getAllContracts();
        return ResponseEntity.ok(result);
    }

    @PostMapping("")
    public ResponseEntity<?> saveContract(@RequestBody Contract contract) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}