package poly.edu.vantix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractCode(String contractCode);

    boolean existsByContractCodeAndDeletedFalse(String contractCode);

    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH c.position " +
           "WHERE c.id = :id AND c.deleted = false")
    Optional<Contract> findActiveById(@Param("id") Long id);

    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH c.position " +
           "WHERE c.deleted = false AND c.employee.id = :employeeId AND c.status = 'ACTIVE' " +
           "ORDER BY c.startDate DESC")
    List<Contract> findActiveContractsByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH c.position " +
           "WHERE c.deleted = false AND c.employee.id = :employeeId " +
           "ORDER BY c.startDate DESC")
    List<Contract> findByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH c.position " +
           "WHERE c.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:employeeId IS NULL OR c.employee.id = :employeeId) " +
           "AND (:contractType IS NULL OR c.contractType = :contractType) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "ORDER BY c.startDate DESC, c.id DESC")
    List<Contract> search(
            @Param("keyword") String keyword,
            @Param("employeeId") Long employeeId,
            @Param("contractType") ContractType contractType,
            @Param("status") ContractStatus status
    );

    @Query(value = "SELECT c FROM Contract c " +
            "LEFT JOIN FETCH c.employee e " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH c.position " +
            "WHERE c.deleted = false " +
            "AND (:keyword IS NULL OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:employeeId IS NULL OR c.employee.id = :employeeId) " +
            "AND (:contractType IS NULL OR c.contractType = :contractType) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.startDate DESC, c.id DESC",
            countQuery = "SELECT COUNT(c) FROM Contract c LEFT JOIN c.employee e " +
                    "WHERE c.deleted = false " +
                    "AND (:keyword IS NULL OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:employeeId IS NULL OR c.employee.id = :employeeId) " +
                    "AND (:contractType IS NULL OR c.contractType = :contractType) " +
                    "AND (:status IS NULL OR c.status = :status)")
    Page<Contract> search(
            @Param("keyword") String keyword,
            @Param("employeeId") Long employeeId,
            @Param("contractType") ContractType contractType,
            @Param("status") ContractStatus status,
            Pageable pageable
    );

    // Tìm hợp đồng sắp hết hạn
    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee " +
           "WHERE c.deleted = false AND c.status = 'ACTIVE' " +
           "AND c.endDate IS NOT NULL " +
           "AND c.endDate BETWEEN :from AND :to " +
           "ORDER BY c.endDate ASC")
    List<Contract> findExpiringContracts(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN FETCH c.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH c.position " +
           "WHERE c.deleted = false AND c.status = 'ACTIVE' " +
           "AND c.endDate IS NOT NULL AND c.endDate < :today")
    List<Contract> findElapsedActiveContracts(@Param("today") LocalDate today);

    // HĐ đang hiệu lực tại một ngày nhất định (dùng cho payroll)
    @Query("SELECT c FROM Contract c " +
           "WHERE c.deleted = false AND c.status = 'ACTIVE' " +
           "AND c.employee.id = :employeeId " +
           "AND c.startDate <= :date " +
           "AND (c.endDate IS NULL OR c.endDate >= :date) " +
           "ORDER BY c.startDate DESC")
    List<Contract> findEffectiveContracts(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    long countByDeletedFalseAndStatus(ContractStatus status);
}
