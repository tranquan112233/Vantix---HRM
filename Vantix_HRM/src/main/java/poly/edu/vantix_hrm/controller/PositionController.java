package poly.edu.vantix_hrm.controller;

import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.repository.PositionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin("*")
public class PositionController {

    private final PositionRepository postionRepository;

    public PositionController(PositionRepository postionRepository) {
        this.postionRepository = postionRepository;
    }

    @GetMapping
    public List<Position> getAll() {
        return postionRepository.findAll();
    }
}