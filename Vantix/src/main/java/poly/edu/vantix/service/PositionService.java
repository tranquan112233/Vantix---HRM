package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.PositionRequest;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.dto.response.PositionResponse;
import poly.edu.vantix.entity.Department;
import poly.edu.vantix.entity.Position;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.DepartmentRepository;
import poly.edu.vantix.repository.PositionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    public PositionService(
            PositionRepository positionRepository,
            DepartmentRepository departmentRepository
    ) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
    }

    // Danh sách chức vụ với tìm kiếm + loc theo phong ban
    @Transactional(readOnly = true)
    public List<PositionResponse> search(String keyword, Long departmentId) {
        return positionRepository.search(keyword, departmentId)
                .stream()
                .map(PositionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PositionResponse> searchPage(String keyword, Long departmentId, Pageable pageable) {
        return PageResponse.from(
                positionRepository.search(keyword, departmentId, pageable),
                PositionResponse::fromEntity
        );
    }

    // Lấy chi tiết chức vụ
    @Transactional(readOnly = true)
    public PositionResponse getById(Long id) {
        Position position = findActiveById(id);
        return PositionResponse.fromEntity(position);
    }

    // Tạo mới chức vụ
    @Transactional
    public PositionResponse create(PositionRequest request) {
        if (positionRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BusinessException("code", "Position code already exists");
        }
        if (positionRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Position name already exists");
        }

        Position position = new Position();
        mapRequestToEntity(request, position);

        position = positionRepository.save(position);
        return PositionResponse.fromEntity(position);
    }

    // Cập nhật chức vụ
    @Transactional
    public PositionResponse update(Long id, PositionRequest request) {
        Position position = findActiveById(id);

        // Kiểm tra trùng code (trừ chính nó)
        positionRepository.findByCode(request.getCode())
                .filter(p -> !p.getId().equals(id) && !p.getDeleted())
                .ifPresent(p -> {
                    throw new BusinessException("code", "Position code already exists");
                });

        mapRequestToEntity(request, position);
        position = positionRepository.save(position);
        return PositionResponse.fromEntity(position);
    }

    // Xóa mềm chức vụ
    @Transactional
    public void delete(Long id) {
        Position position = findActiveById(id);
        position.setDeleted(true);
        position.setDeletedAt(LocalDateTime.now());
        positionRepository.save(position);
    }

    // --- Helper ---

    private Position findActiveById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Position not found with id: " + id));
        if (position.getDeleted()) {
            throw new BusinessException("Position has been deleted");
        }
        return position;
    }

    private void mapRequestToEntity(PositionRequest request, Position position) {
        position.setCode(request.getCode());
        position.setName(request.getName());
        position.setDescription(request.getDescription());

        // Gán phòng ban
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("departmentId", "Department does not exist"));
            position.setDepartment(dept);
        } else {
            position.setDepartment(null);
        }
    }
}
