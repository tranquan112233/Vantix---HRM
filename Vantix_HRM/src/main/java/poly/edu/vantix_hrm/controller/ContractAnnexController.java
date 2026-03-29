package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexRequestDTO;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexResponseDTO;
import poly.edu.vantix_hrm.dto.contractannex.PositionsContractAnnexResponseDTO;
import poly.edu.vantix_hrm.entity.ContractAnnexes;
import poly.edu.vantix_hrm.service.ContractAnnexService;

import java.util.List;

@RestController
@RequestMapping("/api/contract-annexes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ContractAnnexController {

    private final ContractAnnexService contractAnnexService;

    // Lấy toàn bộ thông tin phụ lục kèm logic fallback của 1 hợp đồng
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ContractAnnexResponseDTO> getAnnexesByContract(@PathVariable Integer contractId) {
        ContractAnnexResponseDTO response = contractAnnexService.getAnnexesByContractId(contractId);
        return ResponseEntity.ok(response);
    }

    // API Tạo mới phụ lục
    @PostMapping
    public ResponseEntity<?> createAnnex(@RequestBody ContractAnnexRequestDTO request) {
        try {
            // Không gán vào biến savedAnnex nữa, chỉ gọi hàm chạy
            contractAnnexService.createAnnex(request);

            // Trả về chuỗi Text an toàn để tránh vòng lặp JSON
            return ResponseEntity.ok("Tạo phụ lục thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // API Lấy danh sách tên chức vụ cho Dropdown
    @GetMapping("/positions")
    public ResponseEntity<List<PositionsContractAnnexResponseDTO>> getPositionsForAnnex() {
        return ResponseEntity.ok(contractAnnexService.getAllPositionNames());
    }

    // API đổi trạng thái phụ lục
    @PutMapping("/{annexId}/status")
    public ResponseEntity<?> updateAnnexStatus(@PathVariable Integer annexId) {
        try {
            contractAnnexService.updateAnnexStatus(annexId);
            return ResponseEntity.ok("Cập nhật trạng thái phụ lục thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi cập nhật trạng thái");
        }
    }

    // API Xóa phụ lục
    @DeleteMapping("/{annexId}")
    public ResponseEntity<?> deleteAnnex(@PathVariable Integer annexId) {
        try {
            contractAnnexService.deleteAnnex(annexId);
            return ResponseEntity.ok("Xóa phụ lục thành công.");
        } catch (RuntimeException e) {
            // Hứng lỗi và ném về FE dạng Text
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi hệ thống xảy ra khi xóa phụ lục.");
        }
    }
}
