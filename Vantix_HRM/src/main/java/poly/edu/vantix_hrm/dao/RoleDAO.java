package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.relation.Role;
import java.io.Serializable;

public interface RoleDAO extends JpaRepository<Role, Integer> {

}
