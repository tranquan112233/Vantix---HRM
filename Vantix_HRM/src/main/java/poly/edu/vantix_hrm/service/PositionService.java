package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.position.*;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.PositionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    // ================= FIND ALL =================
    public List<PositionResponse> findAll() {
        return positionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= FIND BY ID =================
    public PositionResponse findById(Integer id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("position", "Position not found"));

        return mapToResponse(position);
    }

    // ================= CREATE =================
    public PositionResponse create(PositionRequest request) {

        String name = request.getPositionName().trim();

        if (positionRepository.existsByPositionName(name)) {
            throw new BusinessException("position", "Position name already exists");
        }

        Position position = new Position();
        position.setPositionName(name);

        positionRepository.save(position);

        return mapToResponse(position);
    }

    // ================= UPDATE =================
    public PositionResponse update(Integer id, PositionRequest request) {

        Position position = positionRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("position", "Position not found"));

        String name = request.getPositionName().trim();

        if (!position.getPositionName().equals(name)
                && positionRepository.existsByPositionName(name)) {

            throw new BusinessException("position", "Position name already exists");
        }

        position.setPositionName(name);
        positionRepository.save(position);

        return mapToResponse(position);
    }

    // ================= DELETE =================
    public void delete(Integer id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("position", "Position not found"));

        positionRepository.delete(position);
    }

    // ================= MAP =================
    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .positionId(position.getPositionId())
                .positionName(position.getPositionName())
                .build();
    }
}