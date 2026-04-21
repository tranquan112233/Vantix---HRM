package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.ShiftRequest;
import poly.edu.vantix.dto.response.ShiftResponse;
import poly.edu.vantix.entity.Shift;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.ShiftRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository repository;

    public ShiftService(ShiftRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> search(String keyword) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(normalized).stream()
                .map(ShiftResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShiftResponse getById(Long id) {
        return ShiftResponse.fromEntity(findActive(id));
    }

    @Transactional
    public ShiftResponse create(ShiftRequest request) {
        String code = request.getCode().trim();
        if (repository.existsByCodeAndDeletedFalse(code)) {
            throw new BusinessException("code", "Shift code already exists");
        }
        Shift shift = new Shift();
        applyRequest(request, shift);
        return ShiftResponse.fromEntity(repository.save(shift));
    }

    @Transactional
    public ShiftResponse update(Long id, ShiftRequest request) {
        Shift shift = findActive(id);
        String code = request.getCode().trim();
        if (!shift.getCode().equalsIgnoreCase(code)
                && repository.existsByCodeAndDeletedFalse(code)) {
            throw new BusinessException("code", "Shift code already exists");
        }
        applyRequest(request, shift);
        return ShiftResponse.fromEntity(repository.save(shift));
    }

    @Transactional
    public void delete(Long id) {
        Shift shift = findActive(id);
        shift.setDeleted(true);
        shift.setDeletedAt(LocalDateTime.now());
        repository.save(shift);
    }

    private void applyRequest(ShiftRequest request, Shift shift) {
        if (request.getStartTime().equals(request.getEndTime())) {
            throw new BusinessException("endTime", "End time must be different from start time");
        }
        shift.setCode(request.getCode().trim().toUpperCase());
        shift.setName(request.getName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setDescription(request.getDescription());
    }

    private Shift findActive(Long id) {
        return repository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Shift does not exist"));
    }
}
