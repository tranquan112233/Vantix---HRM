package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.entity.Roles;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    // Lấy tất cả role
    @GetMapping
    public List<Roles> getAll() {
        return roleRepository.findAll();
    }

    // Lấy role theo ID
    @GetMapping("/{id}")
    public Roles getById(@PathVariable Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    // Thêm mới role
    @PostMapping
    public Roles create(@RequestBody Roles role) {
        return roleRepository.save(role);
    }

    // Cập nhật role
    @PutMapping("/{id}")
    public Roles update(@PathVariable Integer id, @RequestBody Roles role) {

//        role.setRoleID(id);   // ⚠️ QUAN TRỌNG
        return roleRepository.save(role);
    }

    // Xóa role
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleRepository.deleteById(id);
    }
}
