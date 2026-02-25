package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.CreateContractRequest;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.service.ContractsService;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.time.LocalDate;
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

    @PostMapping("/postContract")
    public ResponseEntity<?> saveContract(@Valid @RequestBody CreateContractRequest createRequest) {
        try {
            Employee e = employeeService.isEmployeeValid(createRequest.getEmployeeId());
            contractsService.validateEmployeeContractEligibility(createRequest.getEmployeeId());
            Contract c = new Contract();
            c.setEmployee(e);
            c.setPosition(createRequest.getPosition());
            c.setType(createRequest.getType());
            c.setStartDate(createRequest.getStartDate());
            c.setBaseSalary(createRequest.getBaseSalary());
            c.setStatus(createRequest.getStatus());
            LocalDate endDate = contractsService.calculateEndDateByType(createRequest.getStartDate(), createRequest.getType());
            c.setEndDate(endDate);
            Contract savedContract = contractsService.saveContract(c);
            return ResponseEntity.ok(savedContract);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi lưu hợp đồng: " + e.getMessage());
        }
    }
}