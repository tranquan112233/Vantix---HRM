package poly.edu.vantix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix.dto.request.TaskRequest;
import poly.edu.vantix.dto.request.TaskStatusRequest;
import poly.edu.vantix.dto.response.TaskAttachmentResponse;
import poly.edu.vantix.dto.response.TaskResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.TaskAttachment;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.entity.enums.TaskStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.TaskAttachmentRepository;
import poly.edu.vantix.repository.TaskRepository;
import poly.edu.vantix.repository.UserRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final Path taskUploadRoot;

    public TaskService(
            TaskRepository taskRepository,
            TaskAttachmentRepository taskAttachmentRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            @Value("${app.upload.task-dir:uploads/tasks}") String taskUploadDir
    ) {
        this.taskRepository = taskRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.taskUploadRoot = Paths.get(taskUploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> search(String keyword, TaskStatus status, Long assigneeId) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return taskRepository.search(normalizedKeyword, status, assigneeId)
                .stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return TaskResponse.fromEntity(findActiveTask(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        WorkTask task = new WorkTask();
        mapRequestToTask(request, task);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        WorkTask saved = taskRepository.save(task);
        notifyTaskAssigned(saved);
        return TaskResponse.fromEntity(saved);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        WorkTask task = findActiveTask(id);
        Long previousAssigneeUserId = assigneeUserId(task);
        TaskStatus previousStatus = task.getStatus();
        mapRequestToTask(request, task);
        WorkTask saved = taskRepository.save(task);
        Long currentAssigneeUserId = assigneeUserId(saved);
        boolean statusChanged = previousStatus != saved.getStatus();

        if (!Objects.equals(previousAssigneeUserId, currentAssigneeUserId)) {
            notifyTaskAssigned(saved);
        } else if (!statusChanged && currentAssigneeUserId != null) {
            notificationService.createForUser(
                    currentAssigneeUserId,
                    NotificationType.TASK,
                    "Công việc được cập nhật",
                    "Công việc \"" + saved.getTitle() + "\" vừa được cập nhật.",
                    "/tasks"
            );
        }

        if (statusChanged) {
            notifyTaskStatusChanged(saved, previousStatus);
        }

        return TaskResponse.fromEntity(saved);
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        WorkTask task = findActiveTask(id);
        TaskStatus previousStatus = task.getStatus();
        task.setStatus(request.getStatus());
        WorkTask saved = taskRepository.save(task);
        if (previousStatus != saved.getStatus()) {
            notifyTaskStatusChanged(saved, previousStatus);
        }
        return TaskResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        WorkTask task = findActiveTask(id);
        if (task.getAttachments() != null) {
            task.getAttachments().stream()
                    .filter(attachment -> !Boolean.TRUE.equals(attachment.getDeleted()))
                    .forEach(attachment -> {
                        attachment.setDeleted(true);
                        attachment.setDeletedAt(LocalDateTime.now());
                        deleteStoredFile(attachment);
                    });
        }
        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public List<TaskAttachmentResponse> addAttachments(Long taskId, MultipartFile[] files) {
        WorkTask task = findActiveTask(taskId);

        if (files == null || files.length == 0) {
            throw new BusinessException("files", "At least one file is required");
        }

        List<MultipartFile> validFiles = Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            throw new BusinessException("files", "At least one file is required");
        }

        return validFiles.stream()
                .map(file -> storeAttachment(task, file))
                .map(TaskAttachmentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteAttachment(Long taskId, Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findActiveByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new BusinessException("Attachment does not exist"));

        attachment.setDeleted(true);
        attachment.setDeletedAt(LocalDateTime.now());
        taskAttachmentRepository.save(attachment);
        deleteStoredFile(attachment);
    }

    private void deleteStoredFile(TaskAttachment attachment) {
        try {
            Files.deleteIfExists(resolveStoredPath(attachment.getStoredFileName()));
        } catch (IOException ignored) {
            // Soft delete in database is the source of truth.
        }
    }

    @Transactional(readOnly = true)
    public TaskFileDownload loadAttachment(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findActiveById(attachmentId)
                .orElseThrow(() -> new BusinessException("Attachment does not exist"));

        Path filePath = resolveStoredPath(attachment.getStoredFileName());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("File is not available");
            }

            return new TaskFileDownload(
                    resource,
                    attachment.getOriginalFileName(),
                    attachment.getContentType(),
                    attachment.getFileSize()
            );
        } catch (MalformedURLException ex) {
            throw new BusinessException("File is not available");
        }
    }

    private void mapRequestToTask(TaskRequest request, WorkTask task) {
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setAssignee(resolveAssignee(request.getAssigneeId()));
    }

    private Employee resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        return employeeRepository.findActiveById(assigneeId)
                .orElseThrow(() -> new BusinessException("assigneeId", "Assignee does not exist"));
    }

    private WorkTask findActiveTask(Long id) {
        return taskRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Task does not exist"));
    }

    private void notifyTaskAssigned(WorkTask task) {
        Long assigneeUserId = assigneeUserId(task);
        if (assigneeUserId == null) {
            return;
        }

        String due = task.getDueDate() == null ? "" : " Hạn: " + task.getDueDate() + ".";
        notificationService.createForUser(
                assigneeUserId,
                NotificationType.TASK,
                "Công việc mới được giao",
                "Bạn được giao công việc \"" + task.getTitle() + "\"." + due,
                "/tasks"
        );
    }

    private void notifyTaskStatusChanged(WorkTask task, TaskStatus previousStatus) {
        TaskStatus status = task.getStatus();
        if (status == TaskStatus.DONE) {
            notificationService.createForUsers(
                    taskStatusRecipientIds(task),
                    NotificationType.TASK,
                    "Công việc đã hoàn thành",
                    "Công việc \"" + task.getTitle() + "\" đã được chuyển sang hoàn thành.",
                    "/tasks"
            );
            return;
        }

        if (previousStatus == TaskStatus.DONE) {
            Long assigneeUserId = assigneeUserId(task);
            notificationService.createForUser(
                    assigneeUserId,
                    NotificationType.TASK,
                    "Công việc đã mở lại",
                    "Công việc \"" + task.getTitle() + "\" đã được mở lại.",
                    "/tasks"
            );
        }
    }

    private Set<Long> taskStatusRecipientIds(WorkTask task) {
        Set<Long> userIds = new LinkedHashSet<>();
        Long assigneeUserId = assigneeUserId(task);
        if (assigneeUserId != null) {
            userIds.add(assigneeUserId);
        }
        userRepository.findActiveByAnyPermission(List.of("TASK_UPDATE"))
                .forEach(user -> userIds.add(user.getId()));
        return userIds;
    }

    private Long assigneeUserId(WorkTask task) {
        Employee assignee = task.getAssignee();
        User user = assignee == null ? null : assignee.getUser();
        return user == null ? null : user.getId();
    }

    private TaskAttachment storeAttachment(WorkTask task, MultipartFile file) {
        try {
            Files.createDirectories(taskUploadRoot);

            String originalFileName = sanitizeFileName(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extensionOf(originalFileName);
            Path target = resolveStoredPath(storedFileName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setOriginalFileName(originalFileName);
            attachment.setStoredFileName(storedFileName);
            attachment.setContentType(file.getContentType());
            attachment.setFileSize(file.getSize());
            return taskAttachmentRepository.save(attachment);
        } catch (IOException ex) {
            throw new BusinessException("files", "Unable to store uploaded file");
        }
    }

    private Path resolveStoredPath(String storedFileName) {
        Path target = taskUploadRoot.resolve(storedFileName).normalize();
        if (!target.startsWith(taskUploadRoot)) {
            throw new BusinessException("Invalid file path");
        }
        return target;
    }

    private String sanitizeFileName(String fileName) {
        String value = fileName == null || fileName.isBlank()
                ? "attachment"
                : Paths.get(fileName).getFileName().toString();

        value = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return value.isBlank() ? "attachment" : value;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    public record TaskFileDownload(
            Resource resource,
            String fileName,
            String contentType,
            Long fileSize
    ) {}
}
