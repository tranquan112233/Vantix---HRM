package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dao.ContractsDAO;
import poly.edu.vantix_hrm.entity.Contracts;
import poly.edu.vantix_hrm.service.ContractsService;

import java.util.List;

@Service
public class ContractsServiceImpl implements ContractsService {

    @Autowired
    private ContractsDAO contractsDAO;

    @Override
    public List<Contracts> getAllContracts() {
        return contractsDAO.findAll();
    }
}
