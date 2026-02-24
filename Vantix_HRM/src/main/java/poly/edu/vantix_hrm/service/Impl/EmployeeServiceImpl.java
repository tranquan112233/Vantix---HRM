package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.repository.EmployeesRepository;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.service.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeesRepository employeesRepository;

    @Override
    public Employee isEmployeeValid(Integer employeeId) {
        String msgError = "Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống.";
        Employee employee = employeesRepository.findById(employeeId).orElseThrow(() -> new RuntimeException(msgError));
        return employee;
    }
}
