package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dao.RoleDAO;
import poly.edu.vantix_hrm.entity.Roles;
import poly.edu.vantix_hrm.entity.Users;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RoleController {

    @Autowired
    private RoleDAO roleDAO;

    // Lấy tất cả role
    @GetMapping
    public List<Roles> getAll() {
        return roleDAO.findAll();
    }

    // Lấy role theo ID
    @GetMapping("/{id}")
    public Roles getById(@PathVariable Integer id) {
        return roleDAO.findById(id).orElse(null);
    }

    // Thêm mới role
    @PostMapping
    public Roles create(@RequestBody Roles role) {
        return roleDAO.save(role);
    }

    // Cập nhật role
    @PutMapping("/{id}")
    public Roles update(@PathVariable Integer id, @RequestBody Roles role) {

        role.setRoleID(id);   // ⚠️ QUAN TRỌNG
        return roleDAO.save(role);
    }

    // Xóa role
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleDAO.deleteById(id);
    }
}
