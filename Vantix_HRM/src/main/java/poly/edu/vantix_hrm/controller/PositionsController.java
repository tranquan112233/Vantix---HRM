package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.service.PositionsService;

import java.util.List;

@RestController
@RequestMapping("api/positions")
@CrossOrigin("*")
public class PositionsController {

    @Autowired
    private PositionsService positionsService;

    @GetMapping("getAllPositions")
    public ResponseEntity<?> getAllPositions() {
        List<Position> positionList = positionsService.getAllPositions();
        return ResponseEntity.ok(positionList);
    }
}
