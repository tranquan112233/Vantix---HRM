package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // Tìm user với role và permissions được fetch JOIN để tránh N+1 trong AuthService.me() và JwtFilter
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRoleAndPermissions(@Param("id") Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    // Dùng khi update — kiểm tra trùng nhưng bỏ qua chính nó
    boolean existsByUsernameAndIdNotAndDeletedFalse(String username, Long id);

    boolean existsByEmailAndIdNotAndDeletedFalse(String email, Long id);

}