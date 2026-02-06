package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dao.DepartmentDAO;
import poly.edu.vantix_hrm.entity.Departments;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin("*")
public class DepartmentController {

    @Autowired
    DepartmentDAO departmentDAO;

    // Lấy tất cả department
    @GetMapping
    public List<Departments> findAll() {
        return departmentDAO.findAll();
    }

    // Lấy department theo id
    @GetMapping("/{id}")
    public Departments findById(@PathVariable Integer id) {
        return departmentDAO.findById(id).orElse(null);
    }

    // Thêm mới department
    @PostMapping
    public Departments save(@RequestBody Departments departments) {
        return departmentDAO.save(departments);
    }

    // Cập nhật department
    @PutMapping("/{id}")
    public Departments update(@PathVariable Integer id, @RequestBody Departments departments) {
//        departments.setDepartmentID(id);
        return departmentDAO.save(departments);
    }

    // Xóa department
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        departmentDAO.deleteById(id);
    }
}
