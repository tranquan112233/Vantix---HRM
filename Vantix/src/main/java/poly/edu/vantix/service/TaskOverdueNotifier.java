package poly.edu.vantix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.repository.TaskRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TaskOverdueNotifier {

    private static final Logger log = LoggerFactory.getLogger(TaskOverdueNotifier.class);

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskOverdueNotifier(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void notifyOverdueTasks() {
        LocalDate today = LocalDate.now();
        List<WorkTask> tasks = taskRepository.findTasksNeedingOverdueNotification(today);
        if (tasks.isEmpty()) {
            return;
        }

        log.info("Sending overdue notifications for {} task(s)", tasks.size());
        for (WorkTask task : tasks) {
            Long userId = task.getAssignee().getUser().getId();
            String title;
            String message;
            if (task.getDueDate().isEqual(today)) {
                title = "Công việc đến hạn hôm nay";
                message = String.format("Công việc \"%s\" có hạn xử lý là hôm nay.", task.getTitle());
            } else {
                long days = ChronoUnit.DAYS.between(task.getDueDate(), today);
                title = "Công việc quá hạn";
                message = String.format("Công việc \"%s\" đã quá hạn %d ngày (hạn: %s).",
                        task.getTitle(), days, task.getDueDate());
            }

            notificationService.createForUser(
                    userId,
                    NotificationType.TASK,
                    title,
                    message,
                    "/tasks"
            );

            task.setLastOverdueNotifiedAt(today);
        }
        taskRepository.saveAll(tasks);
    }
}
