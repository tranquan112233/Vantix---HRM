package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Contracts;

@Repository
public interface ContractsDAO extends JpaRepository<Contracts, Integer> {
}
