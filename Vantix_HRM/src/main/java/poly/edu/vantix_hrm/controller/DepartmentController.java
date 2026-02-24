package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.entity.Department;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin("*")
public class DepartmentController {

    @Autowired
    DepartmentRepository departmentRepository;

    // Lấy tất cả department
    @GetMapping
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    // Lấy department theo id
    @GetMapping("/{id}")
    public Department findById(@PathVariable Integer id) {
        return departmentRepository.findById(id).orElse(null);
    }

    // Thêm mới department
    @PostMapping
    public Department save(@RequestBody Department department) {
        return departmentRepository.save(department);
    }

    // Cập nhật department
    @PutMapping("/{id}")
    public Department update(@PathVariable Integer id, @RequestBody Department department) {
//        departments.setDepartmentID(id);
        return departmentRepository.save(department);
    }

    // Xóa department
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        departmentRepository.deleteById(id);
    }
}
