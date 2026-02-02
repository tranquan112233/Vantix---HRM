package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.service.AttendanceService;

@RestController
@RequestMapping("api/attendance")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/create")
    public Attendance save(@RequestBody Integer UserID) {
        return attendanceService.checkIn(UserID);
    }


}
