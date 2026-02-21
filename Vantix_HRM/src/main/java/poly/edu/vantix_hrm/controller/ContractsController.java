package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix_hrm.entity.Contracts;
import poly.edu.vantix_hrm.service.ContractsService;

import java.util.List;

@RestController
@RequestMapping("api/contracts")
@CrossOrigin("*")
public class ContractsController {

    @Autowired
    private ContractsService contractsService;

    @GetMapping("/getAllContracts")
    public ResponseEntity<List<Contracts>> getAllContracts() {
        List<Contracts> result = contractsService.getAllContracts();
        return ResponseEntity.ok(result);
    }
}
