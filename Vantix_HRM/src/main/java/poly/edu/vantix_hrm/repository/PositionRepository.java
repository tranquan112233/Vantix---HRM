package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Position;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    boolean existsByPositionName(String positionName);

    Optional<Position> findByPositionName(String positionName);
}
