package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix.entity.Permission;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
}
