package poly.edu.vantix_hrm.controller;
;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    UserRepository userRepository;

    // Lấy tất cả user
    @GetMapping
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // Lấy user theo id
    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    // Thêm mới user
    @PostMapping
    public User save(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public User update(@PathVariable Integer id, @RequestBody User user) {
//        user.setUserID(id);
        return userRepository.save(user);
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        userRepository.deleteById(id);
    }
}
