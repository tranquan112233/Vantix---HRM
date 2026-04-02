package poly.edu.vantix_hrm.repository;

public interface MyTaskProjection {
    Long getTaskId();
    String getTaskTitle();
    String getDescription();
    Integer getDifficultyLevel();
    Integer getPoint();
    String getStatus();
    Integer getProgressPercent();
}