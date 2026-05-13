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
import poly.edu.vantix.dto.request.EmployeeRequest;
import poly.edu.vantix.dto.response.EmployeeDocumentResponse;
import poly.edu.vantix.dto.response.EmployeeResponse;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;
import poly.edu.vantix.service.EmployeeService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Danh sách nhân viên + tìm kiếm
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(employeeService.searchPage(
                    keyword,
                    departmentId,
                    gender,
                    status,
                    PageableUtils.from(page, size)
            ));
        }

        return ResponseEntity.ok(employeeService.search(keyword, departmentId, gender, status));
    }

    // Chi tiết nhân viên
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // Tạo mới nhân viên
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // Cập nhật nhân viên
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<EmployeeResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(employeeService.uploadPhoto(id, file));
    }

    @GetMapping("/{id}/photo")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long id) {
        EmployeeService.EmployeeFileDownload file = employeeService.loadPhoto(id);
        return fileResponse(file, true);
    }

    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        employeeService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<List<EmployeeDocumentResponse>> uploadDocuments(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addDocuments(id, files));
    }

    @GetMapping("/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        EmployeeService.EmployeeFileDownload file = employeeService.loadDocument(documentId);
        return fileResponse(file, false);
    }

    @DeleteMapping("/{employeeId}/documents/{documentId}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long employeeId,
            @PathVariable Long documentId
    ) {
        employeeService.deleteDocument(employeeId, documentId);
        return ResponseEntity.noContent().build();
    }

    // Xóa mềm nhân viên
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> fileResponse(EmployeeService.EmployeeFileDownload file, boolean inline) {
        String contentType = file.contentType() == null || file.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.fileSize() == null ? -1 : file.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.fileName(), inline))
                .body(file.resource());
    }

    private String contentDisposition(String fileName, boolean inline) {
        String safeFileName = fileName == null ? "employee-file" : fileName.replace("\"", "");
        return (inline ? "inline" : "attachment") + "; filename=\"" + safeFileName + "\"";
    }
}
