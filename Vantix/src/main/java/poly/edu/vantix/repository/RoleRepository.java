package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.id DESC")
    List<Role> search(@Param("keyword") String keyword);

    @Query("SELECT r FROM Role r WHERE r.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.id DESC")
    Page<Role> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);
}
