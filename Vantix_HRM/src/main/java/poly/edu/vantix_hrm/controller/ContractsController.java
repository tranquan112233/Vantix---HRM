package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.contract.CreateContractRequest;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.service.ContractsService;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ContractsController {

    private final ContractsService contractsService;
    private final EmployeeService employeeService;

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        return ResponseEntity.ok(contractsService.getAllContracts());
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> createContract(
            @Valid @RequestBody CreateContractRequest request) {

        try {
            // Validate employee
            Employee employee =
                    employeeService.isEmployeeValid(request.getEmployeeId());

            // Check duplicate active contract
            contractsService
                    .validateEmployeeContractEligibility(request.getEmployeeId());

            // Create contract
            Contract contract = new Contract();
            contract.setEmployee(employee);
            contract.setPosition(request.getPosition());
            contract.setType(request.getType());
            contract.setStartDate(request.getStartDate());
            contract.setBaseSalary(request.getBaseSalary());
            contract.setStatus(request.getStatus());

            LocalDate endDate =
                    contractsService.calculateEndDateByType(
                            request.getStartDate(),
                            request.getType()
                    );

            contract.setEndDate(endDate);

            Contract saved = contractsService.saveContract(contract);

            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi hệ thống khi tạo hợp đồng.");
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{contractId}")
    public ResponseEntity<?> deleteContract(
            @PathVariable Integer contractId) {

        try {
            contractsService.deleteContract(contractId);
            return ResponseEntity.ok("Xóa hợp đồng thành công!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi hệ thống khi xóa hợp đồng.");
        }
    }

    // ================= UPDATE STATUS =================
    @PutMapping("/{contractId}/status")
    public ResponseEntity<?> updateContractStatus(
            @PathVariable Integer contractId) {

        try {
            Contract updated =
                    contractsService.updateContractStatus(contractId);

            String msg = "Hợp đồng (" + updated.getContractId()
                    + ") được cập nhật trạng thái thành ("
                    + updated.getStatus() + ")";

            return ResponseEntity.ok(msg);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi hệ thống khi cập nhật trạng thái.");
        }
    }
}