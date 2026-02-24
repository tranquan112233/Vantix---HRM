package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.repository.PositionsRepository;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.service.PositionsService;

import java.util.List;

@Service
public class PositionsServiceImpl implements PositionsService {

    @Autowired
    private PositionsRepository positionsRepository;

    @Override
    public List<Position> getAllPositions() {
        return positionsRepository.findAll();
    }
}
