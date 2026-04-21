package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix.dto.request.TaskRequest;
import poly.edu.vantix.dto.request.TaskStatusRequest;
import poly.edu.vantix.dto.response.TaskAttachmentResponse;
import poly.edu.vantix.dto.response.TaskResponse;
import poly.edu.vantix.entity.enums.TaskStatus;
import poly.edu.vantix.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId
    ) {
        return ResponseEntity.ok(taskService.search(keyword, status, assigneeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request
    ) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<List<TaskAttachmentResponse>> uploadAttachments(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addAttachments(id, files));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        TaskService.TaskFileDownload file = taskService.loadAttachment(attachmentId);
        String contentType = file.contentType() == null || file.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.fileSize() == null ? -1 : file.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.fileName()))
                .body(file.resource());
    }

    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId
    ) {
        taskService.deleteAttachment(taskId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String contentDisposition(String fileName) {
        String safeFileName = fileName == null ? "attachment" : fileName.replace("\"", "");
        return "attachment; filename=\"" + safeFileName + "\"";
    }
}
