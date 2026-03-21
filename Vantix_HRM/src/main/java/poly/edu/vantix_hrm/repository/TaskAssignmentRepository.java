package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.TaskAssignment;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Integer> {
}