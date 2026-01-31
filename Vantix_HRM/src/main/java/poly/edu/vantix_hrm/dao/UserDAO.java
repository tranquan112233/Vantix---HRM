package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Users;

public interface UserDAO extends JpaRepository<Users,Integer>{
}
