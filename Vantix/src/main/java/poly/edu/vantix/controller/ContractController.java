package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix.dto.request.ContractRenewRequest;
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
    // Kiểm tra quyền của người dùng
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    // Nhận dữ liệu tạo HĐ từ FE -> chuyển sang ContractService.create
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(request));
    }

    @PutMapping("/{id}")
    // Kiểm tra quyền người dùng
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    // Nhận dữ liệu sửa HĐ từ FE -> chuyển sang ContractService.update
    public ResponseEntity<ContractResponse> update(@PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.update(id, request));
    }

    @PostMapping(value = "/{id}/signed-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    // Nhận file PDF đã ký từ FE -> chuyển sang ContractService.uploadSignedFile
    public ResponseEntity<ContractResponse> uploadSignedFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(contractService.uploadSignedFile(id, file));
    }

    @GetMapping("/{id}/signed-file")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<Resource> downloadSignedFile(@PathVariable Long id) {
        ContractService.ContractFileDownload file = contractService.loadSignedFile(id);
        String contentType = file.contentType() == null || file.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.fileSize() == null ? -1 : file.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.fileName()))
                .body(file.resource());
    }

    @DeleteMapping("/{id}/signed-file")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<Void> deleteSignedFile(@PathVariable Long id) {
        contractService.deleteSignedFile(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    // Kiểm tra tài khoản có quyền thực hiện chức năng không
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    // Endpoint "Duyệt HD": FE gọi vào đây để đổi HĐ từ DRAFT sang ACTIVE
    public ResponseEntity<ContractResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.activate(id));
    }

    @PatchMapping("/{id}/renew")
    // Kiểm tra tài khoản có quyền không
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    // Endpoint "Gia hạn HD": FE gửi newEndDate để BE xử lý gia hạn hợp đồng
    public ResponseEntity<ContractResponse> renew( @PathVariable Long id, @Valid @RequestBody ContractRenewRequest request ) {
        return ResponseEntity.ok(contractService.renew(id, request.getNewEndDate()));
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

    @PatchMapping("/{id}/liquidate")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ContractResponse> liquidate(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.liquidate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String contentDisposition(String fileName) {
        String safeFileName = fileName == null ? "contract-file" : fileName.replace("\"", "");
        return "attachment; filename=\"" + safeFileName + "\"";
    }
}
