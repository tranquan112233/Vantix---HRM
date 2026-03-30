package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Permission;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>,
        JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByName(String name);

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByNameAndIdNotAndDeletedFalse(String name, Long id);
}