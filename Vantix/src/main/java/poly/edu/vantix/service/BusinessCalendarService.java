package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.repository.PublicHolidayRepository;
import poly.edu.vantix.repository.WorkScheduleRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class BusinessCalendarService {

    private final PublicHolidayRepository publicHolidayRepository;
    private final WorkScheduleRepository workScheduleRepository;

    public BusinessCalendarService(
            PublicHolidayRepository publicHolidayRepository,
            WorkScheduleRepository workScheduleRepository
    ) {
        this.publicHolidayRepository = publicHolidayRepository;
        this.workScheduleRepository = workScheduleRepository;
    }

    @Transactional(readOnly = true)
    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidayRepository.existsByHolidayDateAndDeletedFalse(date);
    }

    @Transactional(readOnly = true)
    public boolean isWorkingDate(Long employeeId, LocalDate date) {
        if (isPublicHoliday(date)) {
            return false;
        }
        if (employeeId != null && workScheduleRepository.existsByEmployeeIdAndWorkDateAndDeletedFalse(employeeId, date)) {
            return true;
        }
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    @Transactional(readOnly = true)
    public BigDecimal countWorkingLeaveDays(Long employeeId, LocalDate start, LocalDate end, LeaveDayUnit dayUnit) {
        BigDecimal result = BigDecimal.ZERO;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (isWorkingDate(employeeId, date)) {
                result = result.add(BigDecimal.ONE);
            }
            date = date.plusDays(1);
        }
        if (dayUnit == LeaveDayUnit.HALF) {
            return result.multiply(new BigDecimal("0.5"));
        }
        return result;
    }
}
