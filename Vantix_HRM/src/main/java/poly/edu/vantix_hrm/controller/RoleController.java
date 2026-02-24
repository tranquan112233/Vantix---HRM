package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<Role> getAll() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleService.findById(id);
    }

    @PostMapping
    public Role create(@Valid @RequestBody Role role) {
        return roleService.create(role);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }
}
