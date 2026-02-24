package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee; // Chú ý: Dùng Employee (không s)
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
            if (contract.getEmployee() == null || contract.getEmployee().getEmployeeId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu thông tin ID nhân viên.");
            }

            Integer employeeId = contract.getEmployee().getEmployeeId();

            // 2. Kiểm tra nhân viên có tồn tại trong database không
            Employee employee = employeeService.isEmployeeValid(employeeId);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không tìm thấy nhân viên với ID: " + employeeId);
            }

            // 3. Gọi Service để tính toán logic và lưu xuống DB (Truyền từng tham số theo đúng service của bạn)
            // Lưu ý: Đảm bảo ContractsService của bạn dùng Contract.Type và Contract.ContractStatus nhé
            Contract newContract = contractsService.saveContract(
                    employee,
                    contract.getType(),
                    contract.getPosition(),
                    contract.getStartDate(),
                    contract.getBaseSalary(),
                    contract.getStatus()
            );

            return ResponseEntity.ok(newContract);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lưu hợp đồng: " + e.getMessage());
        }
    }
}