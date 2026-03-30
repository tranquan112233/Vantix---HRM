package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>,
        JpaSpecificationExecutor<Role> {

    Optional<Role> findByName(String name);

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByNameAndIdNotAndDeletedFalse(String name, Long id);
}