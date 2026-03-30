package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.position.PositionRequestDTO;
import poly.edu.vantix_hrm.dto.position.PositionResponseDTO;
import poly.edu.vantix_hrm.service.PositionService;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    // GET /api/positions?keyword=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<PageResponseDTO<PositionResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(positionService.getAll(keyword, pageRequest));
    }

    // GET /api/positions/department/{departmentId}
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<PositionResponseDTO>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(positionService.getByDepartment(departmentId));
    }

    // GET /api/positions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PositionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.getById(id));
    }

    // POST /api/positions
    @PostMapping
    public ResponseEntity<PositionResponseDTO> create(@Valid @RequestBody PositionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(request));
    }

    // PUT /api/positions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PositionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequestDTO request) {
        return ResponseEntity.ok(positionService.update(id, request));
    }

    // DELETE /api/positions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}