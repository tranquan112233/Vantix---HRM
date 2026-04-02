package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexResponseDTO;
import poly.edu.vantix_hrm.dto.contractannex.PositionsContractAnnexResponseDTO;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.ContractAnnexes;
import poly.edu.vantix_hrm.repository.ContractAnnexesRepository;
import poly.edu.vantix_hrm.repository.ContractsRepository;
import poly.edu.vantix_hrm.repository.PositionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractAnnexService {

    private final ContractAnnexesRepository contractAnnexesRepository;
    private final ContractsRepository contractRepository;
    private final PositionRepository positionRepository;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // ================== CÁC HÀM TÌM KIẾM CƠ BẢN ==================

    // Tìm kiếm hợp đồng gốc theo ID. Ném lỗi nếu không tồn tại trong database.
    public Contract findContractById(Long contractId) {
        return contractRepository.findById(contractId).orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng gốc với ID: " + contractId));
    }

    // Tìm kiếm phụ lục hợp đồng theo ID. Ném lỗi nếu không tồn tại.
    public ContractAnnexes findAnnexById(Long annexId) {
        return contractAnnexesRepository.findById(annexId).orElseThrow(() -> new RuntimeException("Không tìm thấy phụ lục với ID: " + annexId));
    }

    // Lấy danh sách tất cả các tên chức vụ có trong hệ thống (dùng để đổ vào dropdown chọn chức vụ mới).
    public List<PositionsContractAnnexResponseDTO> getAllPositionNames() {
        return positionRepository.findAll().stream().map(position -> new PositionsContractAnnexResponseDTO(position.getName())).collect(Collectors.toList());
    }

    // ================== CÁC HÀM XỬ LÝ DỮ LIỆU (GET) ==================

    // Lấy toàn bộ thông tin phụ lục của một hợp đồng (gồm thông tin nhân viên, lương/chức vụ thực tế và danh sách phụ lục).
    public ContractAnnexResponseDTO getAnnexesByContractId(Long contractId) {
        Contract contract = findContractById(contractId);
        List<ContractAnnexes> annexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(contractId);

        ContractAnnexResponseDTO response = new ContractAnnexResponseDTO();
        response.setContractId(contract.getContractId());

        if (contract.getEmployee() != null) {
            response.setEmployeeId(contract.getEmployee().getId());
            response.setEmployeeName(contract.getEmployee().getFullName());
        } else {
            response.setEmployeeName("Nhân viên không tồn tại");
        }

        // Tính toán chức vụ và lương hiện tại từ phụ lục
        calculateCurrentPositionAndSalary(contract, annexes, response);

        // Map sang DTO
        List<ContractAnnexResponseDTO.AnnexDetailDTO> detailDTOs = annexes.stream().map(this::mapToAnnexDetailDTO).collect(Collectors.toList());
        response.setAnnexes(detailDTOs);

        return response;
    }

    // Hàm phụ trợ: Tính toán mức lương và chức vụ đang được áp dụng hiện tại dựa trên các phụ lục đang 'Active'.
    private void calculateCurrentPositionAndSalary(Contract contract, List<ContractAnnexes> annexes, ContractAnnexResponseDTO response) {
        String currentPosition = contract.getPosition();
        BigDecimal currentSalary = contract.getBaseSalary();
        boolean isPositionUpdated = false;
        boolean isSalaryUpdated = false;

        for (ContractAnnexes annex : annexes) {
            if (Boolean.TRUE.equals(annex.getIsActive())) {
                if (!isPositionUpdated && annex.getNewPositions() != null && !annex.getNewPositions().trim().isEmpty()) {
                    currentPosition = annex.getNewPositions();
                    isPositionUpdated = true;
                }
                if (!isSalaryUpdated && annex.getNewSalary() != null) {
                    currentSalary = annex.getNewSalary();
                    isSalaryUpdated = true;
                }
                if (isPositionUpdated && isSalaryUpdated) break;
            }
        }
        response.setCurrentPosition(currentPosition);
        response.setCurrentSalary(currentSalary);
    }

    // Hàm phụ trợ: Chuyển đổi từ Entity (ContractAnnexes) sang DTO để trả về cho Client.
    private ContractAnnexResponseDTO.AnnexDetailDTO mapToAnnexDetailDTO(ContractAnnexes annex) {
        ContractAnnexResponseDTO.AnnexDetailDTO dto = new ContractAnnexResponseDTO.AnnexDetailDTO();
        dto.setAnnexId(annex.getAnnexId());
        dto.setEffectiveDate(annex.getEffectiveDate());
        dto.setNewSalary(annex.getNewSalary());
        dto.setNewPositions(annex.getNewPositions());
        dto.setContent(annex.getContent());
        dto.setActive(Boolean.TRUE.equals(annex.getIsActive()));
        return dto;
    }

    // ================== CÁC HÀM VALIDATE ==================

    // Kiểm tra tính hợp lệ của nội dung phụ lục: Phải nhập ít nhất lương hoặc chức vụ; Lương mới phải >= 1.000.000 VNĐ.
    public void validateAnnexContent(BigDecimal newSalary, String newPositions) {
        boolean hasSalary = newSalary != null;
        boolean hasPosition = newPositions != null && !newPositions.trim().isEmpty();

        if (!hasSalary && !hasPosition) {
            throw new IllegalArgumentException("Vui lòng nhập ít nhất Mức lương mới hoặc Chức vụ mới!");
        }
        if (hasSalary && newSalary.compareTo(new BigDecimal("1000000")) < 0) {
            throw new IllegalArgumentException("Mức lương mới phải từ 1.000.000 VNĐ trở lên!");
        }
    }

    // Kiểm tra ngày hiệu lực: Không cho phép chọn ngày hiệu lực ở trong quá khứ.
    public void validateEffectiveDateNotPast(LocalDate effectiveDate) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        if (effectiveDate != null && effectiveDate.isBefore(today)) {
            throw new IllegalArgumentException("Không thể chọn ngày hiệu lực trong quá khứ!");
        }
    }

    // Kiểm tra hợp đồng gốc: Nếu hợp đồng đã hết hạn hoặc quá ngày kết thúc thì chặn không cho thao tác thêm/bật phụ lục.
    public void validateContractNotExpired(Contract contract) {
        if (contract.getStatus() != null && "EXPIRED".equalsIgnoreCase(contract.getStatus().toString())) {
            throw new IllegalArgumentException("Hợp đồng gốc đã hết hạn!");
        }
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(LocalDate.now(VIETNAM_ZONE))) {
            throw new IllegalArgumentException("Hợp đồng gốc đã quá hạn ngày kết thúc!");
        }
    }

    // Kiểm tra xung đột: Đảm bảo không có 2 phụ lục cùng loại (cùng đổi lương hoặc cùng đổi chức vụ) đang 'Active' cùng lúc.
    public void validateNoConflictingActiveAnnex(Long contractId, boolean hasSalary, boolean hasPosition, Long excludeAnnexId) {
        List<ContractAnnexes> existingAnnexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(contractId);
        for (ContractAnnexes existing : existingAnnexes) {
            // Bỏ qua chính nó nếu đang update
            if (excludeAnnexId != null && existing.getAnnexId().equals(excludeAnnexId)) continue;

            if (Boolean.TRUE.equals(existing.getIsActive())) {
                if (hasSalary && existing.getNewSalary() != null) {
                    throw new IllegalArgumentException("Đang có một phụ lục LƯƠNG khác đang áp dụng. Vui lòng tắt nó trước!");
                }
                if (hasPosition && existing.getNewPositions() != null && !existing.getNewPositions().trim().isEmpty()) {
                    throw new IllegalArgumentException("Đang có một phụ lục CHỨC VỤ khác đang áp dụng. Vui lòng tắt nó trước!");
                }
            }
        }
    }

    // Xác định trạng thái 'Active' ban đầu: Nếu ngày hiệu lực ở tương lai, tự động ép thành 'False' chờ đến ngày mới bật.
    public boolean determineInitialActiveStatus(LocalDate effectiveDate, boolean requestedActiveStatus) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        // Nếu ngày hiệu lực ở tương lai, tự động ép thành FALSE
        if (effectiveDate != null && effectiveDate.isAfter(today)) {
            return false;
        }
        return requestedActiveStatus;
    }

    // ================== CÁC HÀM THAO TÁC (SAVE, UPDATE, DELETE) ==================

    // Lưu phụ lục mới hoặc cập nhật phụ lục đã có vào database.
    public ContractAnnexes saveAnnex(ContractAnnexes annex) {
        return contractAnnexesRepository.save(annex);
    }

    // Bật/tắt trạng thái 'Active' của phụ lục. Kiểm tra nghiêm ngặt trước khi cho phép bật (TRUE).
    public ContractAnnexes toggleAnnexStatus(ContractAnnexes annex) {
        boolean newStatus = !Boolean.TRUE.equals(annex.getIsActive());

        if (newStatus) { // Đang cố bật từ FALSE -> TRUE
            LocalDate today = LocalDate.now(VIETNAM_ZONE);
            if (annex.getEffectiveDate() != null && annex.getEffectiveDate().isAfter(today)) {
                throw new IllegalArgumentException("Chưa tới ngày hiệu lực (" + annex.getEffectiveDate() + "), không thể Áp dụng!");
            }
            validateContractNotExpired(annex.getContract());

            boolean hasSalary = annex.getNewSalary() != null;
            boolean hasPosition = annex.getNewPositions() != null && !annex.getNewPositions().trim().isEmpty();
            validateNoConflictingActiveAnnex(annex.getContract().getContractId(), hasSalary, hasPosition, annex.getAnnexId());
        }

        annex.setIsActive(newStatus);
        return contractAnnexesRepository.save(annex);
    }

    // Xóa phụ lục. Chỉ cho phép xóa khi phụ lục đang ở trạng thái Tắt (False).
    public void deleteAnnex(ContractAnnexes annex) {
        if (Boolean.TRUE.equals(annex.getIsActive())) {
            throw new IllegalArgumentException("Chỉ được phép xóa phụ lục đang ở trạng thái 'Chờ duyệt / Hủy'.");
        }
        contractAnnexesRepository.delete(annex);
    }

    // ================== HÀM CHO SCHEDULER ==================

    // Hàm tổng hợp chạy định kỳ (Cron Job): Tự động xử lý trạng thái phụ lục mỗi ngày.
    @Transactional
    public void autoProcessScheduledAnnexes() {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        deactivateAnnexesForExpiredContracts();
        activateReadyAnnexes(today);
    }

    // Hàm phụ trợ (Scheduler): Quét và tắt toàn bộ các phụ lục thuộc về những hợp đồng đã hết hạn.
    private void deactivateAnnexesForExpiredContracts() {
        List<ContractAnnexes> activeAnnexesInExpiredContracts = contractAnnexesRepository.findByIsActiveTrueAndContract_Status(Contract.ContractStatus.EXPIRED);
        for (ContractAnnexes annex : activeAnnexesInExpiredContracts) {
            annex.setIsActive(false);
        }
        contractAnnexesRepository.saveAll(activeAnnexesInExpiredContracts);
    }

    // Hàm phụ trợ (Scheduler): Quét và tự động bật các phụ lục đã đến ngày hiệu lực, đồng thời tắt các phụ lục cũ trùng lặp.
    private void activateReadyAnnexes(LocalDate today) {
        List<ContractAnnexes> readyToActivate = contractAnnexesRepository.findByIsActiveFalseAndEffectiveDateLessThanEqualAndContract_StatusNot(today, Contract.ContractStatus.EXPIRED);

        for (ContractAnnexes annexToActivate : readyToActivate) {
            Contract parentContract = annexToActivate.getContract();
            if (parentContract.getEndDate() != null && parentContract.getEndDate().isBefore(today)) continue;

            boolean hasSalary = annexToActivate.getNewSalary() != null;
            boolean hasPosition = annexToActivate.getNewPositions() != null && !annexToActivate.getNewPositions().trim().isEmpty();

            // Tự động tắt các phụ lục cũ trùng lặp
            List<ContractAnnexes> existingAnnexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(parentContract.getContractId());
            for (ContractAnnexes existing : existingAnnexes) {
                if (!existing.getAnnexId().equals(annexToActivate.getAnnexId()) && Boolean.TRUE.equals(existing.getIsActive())) {
                    boolean conflict = (hasSalary && existing.getNewSalary() != null) || (hasPosition && existing.getNewPositions() != null && !existing.getNewPositions().trim().isEmpty());
                    if (conflict) {
                        existing.setIsActive(false);
                        contractAnnexesRepository.save(existing);
                    }
                }
            }
            annexToActivate.setIsActive(true);
            contractAnnexesRepository.save(annexToActivate);
        }
    }
}