package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // ❗ Không cho client tự set status
        user.setStatus(null);
        user.setCreatedAt(null);
        user.setLastLogin(null);

        return userService.create(user);
    }

    @PutMapping("/{id}")
    public User update(
            @PathVariable Integer id,
            @Valid @RequestBody User user
    ) {
        // ❗ Không cho đổi status + password ở đây
        user.setStatus(null);
        user.setPasswordHash(null);

        return userService.update(id, user);
    }

    @PutMapping("/{id}/toggle-lock")
    public void toggleLock(@PathVariable Integer id) {
        userService.lock(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}

