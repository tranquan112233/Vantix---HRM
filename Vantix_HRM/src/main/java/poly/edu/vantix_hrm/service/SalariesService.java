package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.salaries.ResponseDepartmentDTO;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalariesService {

    private final DepartmentRepository departmentRepository;

    /**
     * Lấy danh sáchtên tất cả các phòng ban chưa bị xóa.
     *
     * @return Danh sách ResponseDepartmentDTO chứa tên phòng ban.
     */
    public List<ResponseDepartmentDTO> findDepartmentNames() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            throw new RuntimeException("Không tìm dữ liệu Phòng Ban nào!");
        }
        return departments.stream().map(dept -> ResponseDepartmentDTO.builder().departmentName(dept.getName()).build()).toList();
    }


}