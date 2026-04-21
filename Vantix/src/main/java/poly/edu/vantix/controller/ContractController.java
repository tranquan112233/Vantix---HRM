package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.ContractRequest;
import poly.edu.vantix.dto.response.ContractResponse;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;
import poly.edu.vantix.service.ContractService;
import poly.edu.vantix.util.PageableUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(contractService.searchPage(
                    keyword, employeeId, contractType, status,
                    PageableUtils.from(page, size)
            ));
        }
        return ResponseEntity.ok(contractService.search(keyword, employeeId, contractType, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ContractResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<List<ContractResponse>> byEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(contractService.getByEmployee(employeeId));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<List<ContractResponse>> expiring(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(contractService.getExpiringContracts(days));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ContractResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request
    ) {
        return ResponseEntity.ok(contractService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ContractResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.activate(id));
    }

    @PatchMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ContractResponse> terminate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        LocalDate terminatedDate = null;
        String reason = null;
        if (body != null) {
            Object d = body.get("terminatedDate");
            if (d != null) {
                terminatedDate = LocalDate.parse(d.toString());
            }
            Object r = body.get("reason");
            if (r != null) {
                reason = r.toString();
            }
        }
        return ResponseEntity.ok(contractService.terminate(id, terminatedDate, reason));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
