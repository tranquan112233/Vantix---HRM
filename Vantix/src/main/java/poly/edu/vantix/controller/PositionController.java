package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.PositionRequest;
import poly.edu.vantix.dto.response.PositionResponse;
import poly.edu.vantix.service.PositionService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    // Danh sách chức vụ + tìm kiếm + loc theo phong ban
    @GetMapping
    @PreAuthorize("hasAuthority('POSITION_VIEW')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(positionService.searchPage(keyword, departmentId, PageableUtils.from(page, size)));
        }

        return ResponseEntity.ok(positionService.search(keyword, departmentId));
    }

    // Chi tiết chức vụ
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_VIEW')")
    public ResponseEntity<PositionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.getById(id));
    }

    // Tạo mới chức vụ
    @PostMapping
    @PreAuthorize("hasAuthority('POSITION_CREATE')")
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody PositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(request));
    }

    // Cập nhật chức vụ
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_UPDATE')")
    public ResponseEntity<PositionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(positionService.update(id, request));
    }

    // Xóa mềm chức vụ
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
