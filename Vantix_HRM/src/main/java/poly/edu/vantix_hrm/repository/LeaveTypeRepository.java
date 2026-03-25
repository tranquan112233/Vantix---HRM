package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
    boolean existsByTypeName(String typeName);
}
