package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.ShiftRequest;
import poly.edu.vantix.dto.response.ShiftResponse;
import poly.edu.vantix.service.ShiftService;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService service;

    public ShiftController(ShiftService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SHIFT_VIEW')")
    public ResponseEntity<List<ShiftResponse>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_VIEW')")
    public ResponseEntity<ShiftResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SHIFT_CREATE')")
    public ResponseEntity<ShiftResponse> create(@Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_UPDATE')")
    public ResponseEntity<ShiftResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
