package poly.edu.vantix.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poly.edu.vantix.repository.PublicHolidayRepository;
import poly.edu.vantix.repository.WorkScheduleRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCalendarServiceTests {

    @Mock
    private PublicHolidayRepository publicHolidayRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @InjectMocks
    private BusinessCalendarService businessCalendarService;

    @Test
    void scheduledWorkingDateTreatsWeekdayAsWorkdayWithoutExplicitSchedule() {
        LocalDate date = LocalDate.of(2025, 4, 25);

        assertTrue(businessCalendarService.isScheduledWorkingDate(1L, date));
    }

    @Test
    void workingDateStillTreatsPublicHolidayAsNonWorkingDay() {
        LocalDate date = LocalDate.of(2025, 4, 30);
        when(publicHolidayRepository.existsByHolidayDateAndDeletedFalse(date)).thenReturn(true);

        assertFalse(businessCalendarService.isWorkingDate(1L, date));
        assertTrue(businessCalendarService.isScheduledWorkingDate(1L, date));
    }

    @Test
    void scheduledWorkingDateTreatsWeekendAsOffWhenNoExplicitSchedule() {
        LocalDate date = LocalDate.of(2025, 4, 26);

        assertFalse(businessCalendarService.isScheduledWorkingDate(1L, date));
    }
}
