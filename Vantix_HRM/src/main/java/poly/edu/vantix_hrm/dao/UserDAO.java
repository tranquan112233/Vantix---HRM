package poly.edu.vantix_hrm.dao;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User,Integer>{
}
