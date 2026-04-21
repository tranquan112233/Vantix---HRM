package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.WorkLocationRequest;
import poly.edu.vantix.dto.response.WorkLocationResponse;
import poly.edu.vantix.entity.WorkLocation;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.WorkLocationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkLocationService {

    private final WorkLocationRepository repository;

    public WorkLocationService(WorkLocationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<WorkLocationResponse> search(String keyword) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(normalized).stream()
                .map(WorkLocationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkLocationResponse getById(Long id) {
        return WorkLocationResponse.fromEntity(findActive(id));
    }

    @Transactional
    public WorkLocationResponse create(WorkLocationRequest request) {
        WorkLocation location = new WorkLocation();
        applyRequest(request, location);
        return WorkLocationResponse.fromEntity(repository.save(location));
    }

    @Transactional
    public WorkLocationResponse update(Long id, WorkLocationRequest request) {
        WorkLocation location = findActive(id);
        applyRequest(request, location);
        return WorkLocationResponse.fromEntity(repository.save(location));
    }

    @Transactional
    public void delete(Long id) {
        WorkLocation location = findActive(id);
        location.setDeleted(true);
        location.setDeletedAt(LocalDateTime.now());
        repository.save(location);
    }

    private void applyRequest(WorkLocationRequest request, WorkLocation location) {
        location.setName(request.getName().trim());
        location.setAddress(request.getAddress());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setRadiusMeters(request.getRadiusMeters());
    }

    private WorkLocation findActive(Long id) {
        return repository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Work location does not exist"));
    }
}
