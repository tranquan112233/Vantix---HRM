package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.repository.DepartmentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class SalariesService {

    private final DepartmentRepository departmentRepository;


}
