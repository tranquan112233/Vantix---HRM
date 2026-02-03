package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Attendance;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceDAO extends JpaRepository<Attendance, Integer> {

    @Query("SELECT a FROM Attendance a WHERE a.user.userID = :userId AND MONTH(a.workDate) = :month AND YEAR(a.workDate) = :year")
    List<Attendance> findAttendanceByMonth(@Param("userId") Integer userId, @Param("month") int month, @Param("year") int year);

    boolean existsByUser_UserIDAndShift_ShiftIDAndWorkDate(Integer userId, Integer shiftId, LocalDate workDate);
}
