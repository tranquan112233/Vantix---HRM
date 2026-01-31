package poly.edu.vantix_hrm.controller;
;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dao.UserDAO;
import poly.edu.vantix_hrm.entity.Users;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    UserDAO userDAO;

    // Lấy tất cả user
    @GetMapping
    public List<Users> getAll() {
        return userDAO.findAll();
    }

    // Lấy user theo id
    @GetMapping("/{id}")
    public Users getById(@PathVariable Integer id) {
        return userDAO.findById(id).orElse(null);
    }

    // Thêm mới user
    @PostMapping
    public Users save(@RequestBody Users user) {
        return userDAO.save(user);
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public Users update(@PathVariable Integer id, @RequestBody Users user) {
        user.setUserID(id);
        return userDAO.save(user);
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        userDAO.deleteById(id);
    }
}
