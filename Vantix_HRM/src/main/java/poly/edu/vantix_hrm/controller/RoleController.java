package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dao.RoleDAO;
import poly.edu.vantix_hrm.entity.Role;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RoleController {

    @Autowired
    private RoleDAO roleDAO;

    // Lấy tất cả role
    @GetMapping
    public List<Role> getAll() {
        return roleDAO.findAll();
    }

    // Lấy role theo ID
    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleDAO.findById(id).orElse(null);
    }

    // Thêm mới role
    @PostMapping
    public Role create(@RequestBody Role role) {
        return roleDAO.save(role);
    }

    // Cập nhật role
    @PutMapping("/{id}")
    public Role update(@PathVariable Integer id, @RequestBody Role role) {

        role.setRoleID(id);   // ⚠️ QUAN TRỌNG
        return roleDAO.save(role);
    }

    // Xóa role
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleDAO.deleteById(id);
    }
}
