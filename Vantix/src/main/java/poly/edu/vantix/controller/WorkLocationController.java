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
import poly.edu.vantix.dto.request.WorkLocationRequest;
import poly.edu.vantix.dto.response.WorkLocationResponse;
import poly.edu.vantix.service.WorkLocationService;

import java.util.List;

@RestController
@RequestMapping("/api/work-locations")
public class WorkLocationController {

    private final WorkLocationService service;

    public WorkLocationController(WorkLocationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WORK_LOCATION_VIEW')")
    public ResponseEntity<List<WorkLocationResponse>> list(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORK_LOCATION_VIEW')")
    public ResponseEntity<WorkLocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WORK_LOCATION_CREATE')")
    public ResponseEntity<WorkLocationResponse> create(@Valid @RequestBody WorkLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORK_LOCATION_UPDATE')")
    public ResponseEntity<WorkLocationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkLocationRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WORK_LOCATION_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
