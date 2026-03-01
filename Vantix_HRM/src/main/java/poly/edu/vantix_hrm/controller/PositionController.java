package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.position.*;
import poly.edu.vantix_hrm.service.PositionService;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    // ================= GET ALL =================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PositionResponse>> getAll() {
        return ResponseEntity.ok(positionService.findAll());
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(positionService.findById(id));
    }

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionResponse> create(
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(positionService.create(request));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(positionService.update(id, request));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}