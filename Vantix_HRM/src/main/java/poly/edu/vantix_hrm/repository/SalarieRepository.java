package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Salarie;

@Repository
public interface SalarieRepository extends JpaRepository<Salarie, Integer> {
}
