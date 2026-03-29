package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexRequestDTO;
import poly.edu.vantix_hrm.dto.contractannex.ContractAnnexResponseDTO;
import poly.edu.vantix_hrm.dto.contractannex.PositionsContractAnnexResponseDTO;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.ContractAnnexes;
import poly.edu.vantix_hrm.repository.ContractAnnexesRepository;
import poly.edu.vantix_hrm.repository.ContractsRepository;
import poly.edu.vantix_hrm.repository.PositionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractAnnexService {

    private final ContractAnnexesRepository contractAnnexesRepository;
    private final ContractsRepository contractRepository;
    private final PositionRepository positionRepository;

    // Load phụ lục
    public ContractAnnexResponseDTO getAnnexesByContractId(Integer contractId) {
        Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng với ID: " + contractId));

        // Lưu ý: Nhớ đảm bảo Repository đã được thêm sắp xếp theo ca.annexId DESC như hướng dẫn ở trên nhé
        List<ContractAnnexes> annexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(contractId);

        ContractAnnexResponseDTO response = new ContractAnnexResponseDTO();
        response.setContractId(contract.getContractId());

        // 🚨 BẢO VỆ CHỐNG LỖI 500 KHI DỮ LIỆU BỊ RÁCH 🚨
        if (contract.getEmployee() != null) {
            response.setEmployeeId(contract.getEmployee().getEmployeeId());
            response.setEmployeeName(contract.getEmployee().getFullName());
        } else {
            response.setEmployeeId(null);
            response.setEmployeeName("Nhân viên không tồn tại");
        }

        String currentPosition = contract.getPosition();
        BigDecimal currentSalary = contract.getBaseSalary();

        // Thêm 2 biến cờ (flag) để theo dõi xem đã tìm thấy cái mới nhất chưa
        boolean isPositionUpdated = false;
        boolean isSalaryUpdated = false;

        for (ContractAnnexes annex : annexes) {
            // Chỉ xét những phụ lục đang ở trạng thái "Áp dụng"
            if (Boolean.TRUE.equals(annex.getIsActive())) {

                // 1. Tìm chức vụ mới nhất
                if (!isPositionUpdated && annex.getNewPositions() != null && !annex.getNewPositions().trim().isEmpty()) {
                    currentPosition = annex.getNewPositions();
                    isPositionUpdated = true; // Đã tìm thấy chức vụ mới nhất
                }

                // 2. Tìm mức lương mới nhất
                if (!isSalaryUpdated && annex.getNewSalary() != null) {
                    currentSalary = annex.getNewSalary();
                    isSalaryUpdated = true; // Đã tìm thấy mức lương mới nhất
                }

                // 3. Nếu đã tìm đủ cả 2 cái mới nhất rồi thì mới được dừng vòng lặp
                if (isPositionUpdated && isSalaryUpdated) {
                    break;
                }
            }
        }

        response.setCurrentPosition(currentPosition);
        response.setCurrentSalary(currentSalary);

        // Map dữ liệu sang DTO để trả về cho Frontend
        List<ContractAnnexResponseDTO.AnnexDetailDTO> annexDetailDTOs = annexes.stream().map(annex -> {
            ContractAnnexResponseDTO.AnnexDetailDTO dto = new ContractAnnexResponseDTO.AnnexDetailDTO();
            dto.setAnnexId(annex.getAnnexId());
            dto.setEffectiveDate(annex.getEffectiveDate());
            dto.setNewSalary(annex.getNewSalary());
            dto.setNewPositions(annex.getNewPositions());
            dto.setContent(annex.getContent());
            dto.setActive(Boolean.TRUE.equals(annex.getIsActive()));
            return dto;
        }).collect(Collectors.toList());

        response.setAnnexes(annexDetailDTOs);

        return response;
    }

    // Thêm mới phụ lục
    public ContractAnnexes createAnnex(ContractAnnexRequestDTO request) {
        if (request.getContractId() == null) {
            throw new IllegalArgumentException("Thiếu ID hợp đồng!");
        }

        boolean hasSalary = request.getNewSalary() != null;
        boolean hasPosition = request.getNewPositions() != null && !request.getNewPositions().trim().isEmpty();

        if (!hasSalary && !hasPosition) {
            throw new IllegalArgumentException("Vui lòng nhập ít nhất Mức lương mới hoặc Chức vụ mới!");
        }

        // 🚨 ĐIỀU KIỆN KIỂM TRA LƯƠNG TỐI THIỂU 🚨
        if (hasSalary && request.getNewSalary().compareTo(new BigDecimal("1000000")) < 0) {
            throw new IllegalArgumentException("Mức lương mới phải từ 1.000.000 VNĐ trở lên!");
        }

        Contract contract = contractRepository.findById(request.getContractId()).orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng gốc với ID: " + request.getContractId()));

        // 🚨 KIỂM TRA HỢP ĐỒNG HẾT HẠN 🚨
        if (contract.getStatus() != null && "EXPIRED".equalsIgnoreCase(contract.getStatus().toString())) {
            throw new IllegalArgumentException("Hợp đồng gốc đã hết hạn. Không thể tạo thêm phụ lục!");
        }

        // 🌟 BẮT LỖI TRÙNG LẶP PHỤ LỤC ĐANG ÁP DỤNG 🌟
        // Chỉ cần kiểm tra nếu người dùng chọn trạng thái là "Áp dụng ngay" (isActive = true)
        if (request.isActive()) {
            List<ContractAnnexes> existingAnnexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(request.getContractId());

            for (ContractAnnexes annex : existingAnnexes) {
                if (Boolean.TRUE.equals(annex.getIsActive())) {
                    // Nếu đang muốn tạo PL tăng lương, mà đã có 1 PL Lương khác đang active -> CHẶN
                    if (hasSalary && annex.getNewSalary() != null) {
                        throw new IllegalArgumentException("Đang có một phụ lục LƯƠNG khác đang được áp dụng! Vui lòng chuyển phụ lục cũ sang 'Chờ duyệt / Hủy' trước khi áp dụng phụ lục mới.");
                    }
                    // Nếu đang muốn tạo PL chức vụ, mà đã có 1 PL Chức vụ khác đang active -> CHẶN
                    if (hasPosition && annex.getNewPositions() != null && !annex.getNewPositions().trim().isEmpty()) {
                        throw new IllegalArgumentException("Đang có một phụ lục CHỨC VỤ khác đang được áp dụng! Vui lòng chuyển phụ lục cũ sang 'Chờ duyệt / Hủy' trước khi áp dụng phụ lục mới.");
                    }
                }
            }
        }

        ContractAnnexes newAnnex = new ContractAnnexes();
        newAnnex.setContract(contract);
        newAnnex.setEffectiveDate(request.getEffectiveDate());
        newAnnex.setNewSalary(request.getNewSalary());
        newAnnex.setNewPositions(request.getNewPositions());
        newAnnex.setContent(request.getContent());
        newAnnex.setIsActive(request.isActive());

        return contractAnnexesRepository.save(newAnnex);
    }

    // Load Position cho chức năng thêm mới phụ lục
    public List<PositionsContractAnnexResponseDTO> getAllPositionNames() {
        return positionRepository.findAll().stream().map(position -> new PositionsContractAnnexResponseDTO(position.getPositionName())).collect(Collectors.toList());
    }

    // Cập nhật trạng thái Phụ lục (Toggle true/false)
    public void updateAnnexStatus(Integer annexId) {
        ContractAnnexes annexToUpdate = contractAnnexesRepository.findById(annexId).orElseThrow(() -> new RuntimeException("Không tìm thấy phụ lục với ID: " + annexId));

        boolean newStatus = !Boolean.TRUE.equals(annexToUpdate.getIsActive());

        // 🌟 BẮT LỖI TRÙNG LẶP KHI BẬT TRẠNG THÁI TỪ FALSE LÊN TRUE 🌟
        if (newStatus) {
            boolean hasSalary = annexToUpdate.getNewSalary() != null;
            boolean hasPosition = annexToUpdate.getNewPositions() != null && !annexToUpdate.getNewPositions().trim().isEmpty();

            List<ContractAnnexes> existingAnnexes = contractAnnexesRepository.findByContractIdOrderByEffectiveDateDesc(annexToUpdate.getContract().getContractId());

            for (ContractAnnexes existing : existingAnnexes) {
                // Kiểm tra những phụ lục KHÁC với phụ lục mình đang thao tác, xem có thằng nào đang TRUE không
                if (!existing.getAnnexId().equals(annexId) && Boolean.TRUE.equals(existing.getIsActive())) {
                    if (hasSalary && existing.getNewSalary() != null) {
                        throw new IllegalArgumentException("Đã có một phụ lục LƯƠNG khác đang áp dụng. Hãy tắt nó trước khi bật phụ lục này!");
                    }
                    if (hasPosition && existing.getNewPositions() != null && !existing.getNewPositions().trim().isEmpty()) {
                        throw new IllegalArgumentException("Đã có một phụ lục CHỨC VỤ khác đang áp dụng. Hãy tắt nó trước khi bật phụ lục này!");
                    }
                }
            }
        }

        annexToUpdate.setIsActive(newStatus);
        contractAnnexesRepository.save(annexToUpdate);
    }

    // Xóa Phụ lục (Có điều kiện)
    public void deleteAnnex(Integer annexId) {
        ContractAnnexes annex = contractAnnexesRepository.findById(annexId).orElseThrow(() -> new RuntimeException("Không tìm thấy phụ lục với ID: " + annexId));

        // Kiểm tra trạng thái: Nếu đang là TRUE (Áp dụng) thì KHÔNG cho xóa
        // Dùng Boolean.TRUE.equals để tránh lỗi NullPointerException nếu cột này trong DB bị null
        if (Boolean.TRUE.equals(annex.getIsActive())) {
            throw new IllegalArgumentException("Không thể xóa! Chỉ được phép xóa phụ lục đang ở trạng thái 'Áp Dụng'.");
        }

        contractAnnexesRepository.delete(annex);
    }
}