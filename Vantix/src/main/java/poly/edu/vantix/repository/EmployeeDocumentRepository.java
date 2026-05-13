package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.EmployeeDocument;

import java.util.Optional;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    @Query("SELECT d FROM EmployeeDocument d JOIN FETCH d.employee " +
           "WHERE d.id = :id AND d.deleted = false")
    Optional<EmployeeDocument> findActiveById(@Param("id") Long id);

    @Query("SELECT d FROM EmployeeDocument d JOIN FETCH d.employee " +
           "WHERE d.id = :id AND d.employee.id = :employeeId AND d.deleted = false")
    Optional<EmployeeDocument> findActiveByIdAndEmployeeId(
            @Param("id") Long id,
            @Param("employeeId") Long employeeId
    );
}
