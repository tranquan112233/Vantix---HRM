package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Positions;

@Repository
public interface PositionsDAO extends JpaRepository<Positions, Integer> {
    
}
