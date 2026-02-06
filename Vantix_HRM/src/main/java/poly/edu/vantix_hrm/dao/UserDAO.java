package poly.edu.vantix_hrm.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.User;

@Repository
public interface UserDAO extends JpaRepository<User,Integer>{
    User findByUsername(String username);

}
