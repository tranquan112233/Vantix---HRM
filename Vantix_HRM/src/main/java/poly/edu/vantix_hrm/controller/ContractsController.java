package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.contract.ContractResponseDTO;
import poly.edu.vantix_hrm.dto.contract.CreateContractRequest;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.exception.BusinessException;
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

    // Tải toàn bộ Hợp Đồng lên
    @GetMapping("/all")
    public ResponseEntity<List<ContractResponseDTO>> getAllContracts() {
        return ResponseEntity.ok(contractsService.getAllContracts());
    }

    // Tạo mới Hợp Đồng
    @PostMapping("/create")
    public ResponseEntity<?> createContract(@Valid @RequestBody CreateContractRequest request) {
        try {
            // Kiểm tra nhân viên có tồn tại không
            if (!employeeService.isValidEmployee(request.getEmployeeId())) {
                throw new BusinessException("employee","Nhân viên không hợp lệ!", HttpStatus.NOT_FOUND);
            }

            Employee employee = employeeService.getValidEmployee(request.getEmployeeId());

            // Kiểm tra trạng thái Hợp Đồng của nhân viên
            contractsService.validateEmployeeContractEligibility(request.getEmployeeId());

            // Tính EndDate của Hợp Đồng
            LocalDate endDate = contractsService.calculateEndDateByType(request.getStartDate(), request.getType());

            // Tạo mới Hợp Đồng
            Contract contract = new Contract();                 // Khai báo biến lưu
            contract.setEmployee(employee);                     // Gán employee
            contract.setPosition(request.getPosition());        // Gán Position
            contract.setType(request.getType());                // Gán Type
            contract.setStartDate(request.getStartDate());      // Gán StartDate
            contract.setEndDate(endDate);                       // Gán EndDate
            contract.setBaseSalary(request.getBaseSalary());    // Gán BaseSalary
            contract.setStatus(request.getStatus());            // Gán Status

            // Lưu Hợp Đồng
            Contract saved = contractsService.saveContract(contract);

            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi tạo hợp đồng.");
        }
    }

    // Xóa Hợp Đồng
    @DeleteMapping("/{contractId}")
    public ResponseEntity<?> deleteContract(@PathVariable Integer contractId) {
        try {
            // Tìm Hợp Đồng từ mã
            Contract contract = contractsService.findById(contractId);

            // Xóa Hợp Đồng
            contractsService.deleteContract(contract);
            return ResponseEntity.ok("Xóa hợp đồng thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi xóa hợp đồng.");
        }
    }

    // Cập nhật trạng thái Hợp Đồng
    @PutMapping("/{contractId}/status")
    public ResponseEntity<?> updateContractStatus(@PathVariable Integer contractId) {
        try {
            // Tìm Hợp Đồng
            Contract contract = contractsService.findById(contractId);

            // Chỉnh sửa Trạng Thái Hợp Đồng
            Contract updated = contractsService.updateContractStatus(contract);

            String msg = "Hợp đồng (" + updated.getContractId() + ") được cập nhật trạng thái thành (" + updated.getStatus() + ")";

            return ResponseEntity.ok(msg);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi cập nhật trạng thái.");
        }
    }
}