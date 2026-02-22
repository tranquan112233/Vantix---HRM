package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dao.PositionsDAO;
import poly.edu.vantix_hrm.entity.Positions;
import poly.edu.vantix_hrm.service.PositionsService;

import java.util.List;

@Service
public class PositionsServiceImpl implements PositionsService {

    @Autowired
    private PositionsDAO positionsDAO;

    @Override
    public List<Positions> getAllPositions() {
        return positionsDAO.findAll();
    }
}
