package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.role.*;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.RoleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoleResponse findById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("role","Role not found"));

        return mapToResponse(role);
    }

    public RoleResponse create(RoleRequest request) {

        String roleName = request.getRoleName().trim().toUpperCase();

        if (roleRepository.existsByRoleName(roleName)) {
            throw new BusinessException("role","Role name already exists");
        }

        Role role = Role.builder()
                .roleName(roleName)
                .description(request.getDescription())
                .build();

        roleRepository.save(role);

        return mapToResponse(role);
    }

    public RoleResponse update(Integer id, RoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("role","Role not found"));

        String roleName = request.getRoleName().trim().toUpperCase();

        if (!role.getRoleName().equals(roleName)
                && roleRepository.existsByRoleName(roleName)) {

            throw new BusinessException("role","Role name already exists");
        }

        role.setRoleName(roleName);
        role.setDescription(request.getDescription());

        roleRepository.save(role);

        return mapToResponse(role);
    }

    public void delete(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("role","Role not found"));

        roleRepository.delete(role);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }
}