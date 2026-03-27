package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.ContractAnnexes;

@Repository
public interface ContractAnnexesRepository extends JpaRepository<ContractAnnexes, Integer> {

    // Trả về số lượng phụ lục đang liên kết với hợp đồng
    @Query("SELECT COUNT(ca) FROM ContractAnnexes ca WHERE ca.contract.contractId = :contractId")
    long countAnnexesByContractId(@Param("contractId") Integer contractId);
}