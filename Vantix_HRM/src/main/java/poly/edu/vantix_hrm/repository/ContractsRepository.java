package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Contract;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractsRepository extends JpaRepository<Contract, Integer> {

    // Lấy danh sách hợp đồng kèm thông tin nhân viên (tránh lỗi N+1) và sắp xếp ID giảm dần
    @Query("SELECT c FROM Contract c JOIN FETCH c.employee e ORDER BY c.contractId DESC")
    List<Contract> findAllContractsWithEmployee();

    // Trả về số lượng phụ lục đang liên kết với hợp đồng
    @Query("SELECT COUNT(ca) FROM ContractAnnexes ca WHERE ca.contract.contractId = :contractId")
    long countAnnexesByContractId(@Param("contractId") Integer contractId);

    // Các hàm có sẵn của bạn
    List<Contract> findByEmployee_EmployeeId(Integer employeeId);

    List<Contract> findByStatusAndEndDateBefore(Contract.ContractStatus status, LocalDate date);
}