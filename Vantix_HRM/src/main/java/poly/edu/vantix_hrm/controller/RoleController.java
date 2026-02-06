package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.service.RoleService;
import poly.edu.vantix_hrm.dao.RoleDAO;
import poly.edu.vantix_hrm.entity.Role;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

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
        return roleDAO.findAll();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleService.findById(id);
        return roleDAO.findById(id).orElse(null);
    }

    @PostMapping
    public Role create(@Valid @RequestBody Role role) {
        return roleService.create(role);
    public Role create(@RequestBody Role role) {
        return roleDAO.save(role);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Integer id, @RequestBody Role role) {

        role.setRoleID(id);
        return roleDAO.save(role);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }
}
