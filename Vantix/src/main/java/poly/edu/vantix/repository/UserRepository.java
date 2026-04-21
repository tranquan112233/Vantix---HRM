package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findFirstByUsernameOrEmail(String username, String email);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT DISTINCT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findActiveWithRoleAndPermissionsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.role r " +
           "JOIN r.permissions p " +
           "WHERE u.deleted = false " +
           "AND u.status = :status " +
           "AND r.deleted = false " +
           "AND p.name IN :permissions")
    List<User> findByAnyPermissionAndStatus(
            @Param("permissions") Collection<String> permissions,
            @Param("status") UserStatus status
    );

    default List<User> findActiveByAnyPermission(Collection<String> permissions) {
        return findByAnyPermissionAndStatus(permissions, UserStatus.ACTIVE);
    }

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.id DESC")
    List<User> search(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:roleId IS NULL OR u.role.id = :roleId) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.id DESC")
    List<User> search(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("status") UserStatus status
    );

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.id DESC")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:roleId IS NULL OR u.role.id = :roleId) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.id DESC")
    Page<User> search(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    long countByDeletedFalseAndStatus(UserStatus status);
}
