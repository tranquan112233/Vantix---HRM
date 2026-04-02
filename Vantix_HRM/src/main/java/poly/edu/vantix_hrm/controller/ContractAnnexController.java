package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexRequestDTO;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexResponseDTO;
import poly.edu.vantix_hrm.dto.contractannex.PositionsContractAnnexResponseDTO;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.ContractAnnexes;
import poly.edu.vantix_hrm.service.ContractAnnexService;

import java.util.List;

@RestController
@RequestMapping("/api/contract-annexes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ContractAnnexController {

    private final ContractAnnexService contractAnnexService;

    // API: Lấy danh sách phụ lục và thông tin tổng quan dựa vào ID của Hợp đồng gốc.
    @PreAuthorize("hasAuthority('CONTRACT_ANNEX_VIEW')")
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ContractAnnexResponseDTO> getAnnexesByContract(@PathVariable Long contractId) {
        ContractAnnexResponseDTO response = contractAnnexService.getAnnexesByContractId(contractId);
        return ResponseEntity.ok(response);
    }

    // API: Lấy danh sách tên các chức vụ để hiển thị trên form dropdown tạo/sửa phụ lục.
    @PreAuthorize("hasAuthority('CONTRACT_ANNEX_VIEW')")
    @GetMapping("/positions")
    public ResponseEntity<List<PositionsContractAnnexResponseDTO>> getPositionsForAnnex() {
        return ResponseEntity.ok(contractAnnexService.getAllPositionNames());
    }

    // API: Tạo mới một phụ lục hợp đồng. Controller đóng vai trò điều phối các luồng validate từ Service trước khi lưu.
    @PreAuthorize("hasAuthority('CONTRACT_ANNEX_CREATE')")
    @PostMapping
    public ResponseEntity<?> createAnnex(@RequestBody ContractAnnexRequestDTO request) {
        try {
            // 1. Tìm Hợp Đồng xem có tồn tại không
            Contract contract = contractAnnexService.findContractById(request.getContractId());

            // 2. Validate các điều kiện (Nội dung, ngày tháng, hạn hợp đồng)
            contractAnnexService.validateAnnexContent(request.getNewSalary(), request.getNewPositions());
            contractAnnexService.validateEffectiveDateNotPast(request.getEffectiveDate());
            contractAnnexService.validateContractNotExpired(contract);

            // 3. Quyết định trạng thái Active ban đầu (Nếu ngày ở tương lai thì bắt buộc False)
            boolean willBeActive = contractAnnexService.determineInitialActiveStatus(request.getEffectiveDate(), request.isActive());

            // 4. Nếu chuẩn bị Active thì check xem có bị trùng lặp với phụ lục nào đang bật không
            if (willBeActive) {
                boolean hasSalary = request.getNewSalary() != null;
                boolean hasPosition = request.getNewPositions() != null && !request.getNewPositions().trim().isEmpty();
                contractAnnexService.validateNoConflictingActiveAnnex(contract.getContractId(), hasSalary, hasPosition, null);
            }

            // 5. Build đối tượng Entity và Lưu vào DB
            ContractAnnexes newAnnex = new ContractAnnexes();
            newAnnex.setContract(contract);
            newAnnex.setEffectiveDate(request.getEffectiveDate());
            newAnnex.setNewSalary(request.getNewSalary());
            newAnnex.setNewPositions(request.getNewPositions());
            newAnnex.setContent(request.getContent());
            newAnnex.setIsActive(willBeActive);

            contractAnnexService.saveAnnex(newAnnex);

            return ResponseEntity.ok("Tạo phụ lục thành công");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // API: Thay đổi trạng thái (Bật/Tắt) của một phụ lục cụ thể.
    @PreAuthorize("hasAuthority('CONTRACT_ANNEX_UPDATE')")
    @PutMapping("/{annexId}/status")
    public ResponseEntity<?> updateAnnexStatus(@PathVariable Long annexId) {
        try {
            // Tìm phụ lục
            ContractAnnexes annex = contractAnnexService.findAnnexById(annexId);

            // Toggle trạng thái (Service sẽ tự động xử lý validate hợp lệ trước khi cho bật)
            contractAnnexService.toggleAnnexStatus(annex);

            return ResponseEntity.ok("Cập nhật trạng thái phụ lục thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi cập nhật trạng thái");
        }
    }

    // API: Xóa một phụ lục cụ thể ra khỏi hệ thống.
    @PreAuthorize("hasAuthority('CONTRACT_ANNEX_DELETE')")
    @DeleteMapping("/{annexId}")
    public ResponseEntity<?> deleteAnnex(@PathVariable Long annexId) {
        try {
            // Tìm phụ lục
            ContractAnnexes annex = contractAnnexService.findAnnexById(annexId);

            // Xóa (Service sẽ chặn nếu phụ lục đang bật)
            contractAnnexService.deleteAnnex(annex);

            return ResponseEntity.ok("Xóa phụ lục thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi hệ thống xảy ra khi xóa phụ lục.");
        }
    }
}