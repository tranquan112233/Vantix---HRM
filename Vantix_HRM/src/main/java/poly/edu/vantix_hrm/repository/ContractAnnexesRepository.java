package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.ContractAnnexes;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractAnnexesRepository extends JpaRepository<ContractAnnexes, Long> {

    // Trả về số lượng phụ lục đang liên kết với hợp đồng
    @Query("SELECT COUNT(ca) FROM ContractAnnexes ca WHERE ca.contract.contractId = :contractId")
    long countAnnexesByContractId(@Param("contractId") Long contractId);

    // LẤY DANH SÁCH PHỤ LỤC, SẮP XẾP THEO NGÀY HIỆU LỰC GIẢM DẦN VÀ ID GIẢM DẦN
    @Query("SELECT ca FROM ContractAnnexes ca WHERE ca.contract.contractId = :contractId ORDER BY ca.effectiveDate DESC, ca.annexId DESC")
    List<ContractAnnexes> findByContractIdOrderByEffectiveDateDesc(@Param("contractId") Long contractId);

    // Dành cho Scheduler: Tìm các phụ lục ĐANG ÁP DỤNG nhưng Hợp đồng đã HẾT HẠN
    List<ContractAnnexes> findByIsActiveTrueAndContract_Status(Contract.ContractStatus status);

    // Dành cho Scheduler: Tìm các phụ lục CHƯA ÁP DỤNG, đã ĐẾN NGÀY HIỆU LỰC, và Hợp đồng CHƯA HẾT HẠN
    List<ContractAnnexes> findByIsActiveFalseAndEffectiveDateLessThanEqualAndContract_StatusNot(LocalDate date, Contract.ContractStatus status);
}