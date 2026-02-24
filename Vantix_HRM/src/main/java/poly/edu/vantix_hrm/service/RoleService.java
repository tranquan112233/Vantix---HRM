package poly.edu.vantix_hrm.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.repository.RoleRepository;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Lấy tất cả role
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    // Lấy role theo id
    public Role findById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Role not found"
                        ));
    }

    // Tạo role
    public Role create(Role role) {

        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Role name already exists"
            );
        }

        return roleRepository.save(role);
    }

    // Update role
    public Role update(Integer id, Role role) {
        Role existing = findById(id);

        if (!existing.getRoleName().equals(role.getRoleName())
                && roleRepository.existsByRoleName(role.getRoleName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Role name already exists"
            );
        }

        existing.setRoleName(role.getRoleName());
        existing.setDescription(role.getDescription());

        return roleRepository.save(existing);
    }

    // Xóa role
    public void delete(Integer id) {
        Role role = findById(id);
        roleRepository.delete(role);
    }
}
