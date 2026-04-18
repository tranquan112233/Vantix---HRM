package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.schedule.DailyScheduleDTO;
import poly.edu.vantix_hrm.dto.schedule.EmployeeScheduleDTO;
import poly.edu.vantix_hrm.dto.schedule.MonthlyScheduleDTO;
import poly.edu.vantix_hrm.entity.DailyWorkSchedules;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.MonthlySchedules;
import poly.edu.vantix_hrm.entity.Shift;
import poly.edu.vantix_hrm.repository.DailyWorkSchedulesRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.MonthlySchedulesRepository;
import poly.edu.vantix_hrm.repository.ShiftsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final EmployeeRepository employeeRepository;
    private final MonthlySchedulesRepository monthlySchedulesRepository;
    private final DailyWorkSchedulesRepository dailyWorkSchedulesRepository;
    private final ShiftsRepository shiftsRepository;
    private final NotificationService notificationService;

    public List<EmployeeScheduleDTO> getSchedulesForUser(Long viewerId, int month, int year) {
        String msgErrorEmployeeNotFound = "Không tìm thấy nhân viên với ID:" + viewerId;
        Employee viewer = employeeRepository.findById(viewerId).orElseThrow(() -> new RuntimeException(msgErrorEmployeeNotFound));

        List<EmployeeScheduleDTO> result = new ArrayList<>();

        // Xác định xem người đang xem có phải Manager/Admin không (dựa trên Role)
        boolean isManager = viewer.getUser() != null
                && viewer.getUser().getRole() != null
                && ("DEPARTMENT_MANAGER".equalsIgnoreCase(viewer.getUser().getRole().getName())
                || "HR_MANAGER".equalsIgnoreCase(viewer.getUser().getRole().getName())
                || "ADMIN".equalsIgnoreCase(viewer.getUser().getRole().getName()));

        if (isManager) {
            List<Employee> staffList = employeeRepository.findByDepartment_Id(viewer.getDepartment().getId());
            for (Employee staff : staffList) {
                EmployeeScheduleDTO dto = buildEmployeeScheduleDTO(staff, month, year, isManager);
                // Đánh dấu viewer là manager trong response của chính họ
                if (staff.getId().equals(viewerId)) {
                    dto.setManager(true);
                }
                result.add(dto);
            }
        } else {
            result.add(buildEmployeeScheduleDTO(viewer, month, year, isManager));
        }
        return result;
    }

    // Cập nhật lại hàm này: Thêm tham số boolean isManager
    private EmployeeScheduleDTO buildEmployeeScheduleDTO(Employee emp, int month, int year, boolean isManager) {
        EmployeeScheduleDTO dto = new EmployeeScheduleDTO();
        dto.setEmployeeId(emp.getId());
        dto.setFullName(emp.getFullName());

        MonthlySchedules monthlyEntity = monthlySchedulesRepository.findByEmployee_IdAndMonthAndYear(emp.getId(), month, year);

        // LOGIC LỌC DỮ LIỆU Ở ĐÂY:
        // Nếu có lịch, NHƯNG người xem là Nhân Viên VÀ lịch vẫn đang OPEN -> Coi như chưa có lịch
        if (monthlyEntity != null) {
            if (!isManager && monthlyEntity.getStatus() == MonthlySchedules.ScheduleStatus.OPEN) {
                // Đang là bản nháp -> Không trả về dailySchedules cho nhân viên thấy
                return dto;
            }

            // Nếu là Trưởng phòng hoặc lịch đã LOCKED -> Điền dữ liệu bình thường
            MonthlyScheduleDTO monthlyDTO = new MonthlyScheduleDTO();
            monthlyDTO.setMonthlyScheduleId(monthlyEntity.getMonthlyScheduleId());
            monthlyDTO.setMonth(monthlyEntity.getMonth());
            monthlyDTO.setYear(monthlyEntity.getYear());
            monthlyDTO.setStatus(monthlyEntity.getStatus() != null ? monthlyEntity.getStatus().name() : null);

            List<DailyWorkSchedules> dailyEntities = dailyWorkSchedulesRepository.findByMonthlySchedule_MonthlyScheduleId(monthlyEntity.getMonthlyScheduleId());

            List<DailyScheduleDTO> dailyDTOs = dailyEntities.stream().map(daily -> {
                DailyScheduleDTO dDto = new DailyScheduleDTO();
                dDto.setDailyScheduleId(daily.getDailyScheduleId());
                dDto.setMonthlyScheduleId(monthlyEntity.getMonthlyScheduleId());
                dDto.setWorkDate(daily.getWorkDate());
                if (daily.getShift() != null) {
                    dDto.setShiftId(daily.getShift().getShiftId());
                    dDto.setShiftName(daily.getShift().getShiftName());
                }
                dDto.setDayType(daily.getDayType() != null ? daily.getDayType().name() : null);
                return dDto;
            }).collect(Collectors.toList());

            monthlyDTO.setDailySchedules(dailyDTOs);
            dto.setMonthlySchedule(monthlyDTO);
        }
        return dto;
    }

    public void saveDailySchedules(Long employeeId, int month, int year, List<DailyScheduleDTO> incomingDays) {

        // 1. Tìm xem nhân viên này đã có lịch của tháng/năm này chưa?
        MonthlySchedules monthEntity = monthlySchedulesRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year);

        // 2. NẾU CHƯA CÓ: Tự động tạo mới một bản ghi lịch tháng
        if (monthEntity == null) {
            Employee emp = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

            monthEntity = new MonthlySchedules();
            monthEntity.setEmployee(emp);
            monthEntity.setMonth(month);
            monthEntity.setYear(year);
            monthEntity.setStatus(MonthlySchedules.ScheduleStatus.OPEN); // Mặc định là OPEN

            // Lưu xuống DB để nó tự tạo ra khóa chính (monthlyScheduleId)
            monthEntity = monthlySchedulesRepository.save(monthEntity);
        }

        Long monthlyId = monthEntity.getMonthlyScheduleId();

        // 3. Lấy các ngày làm việc hiện có của tháng này lên để so sánh
        List<DailyWorkSchedules> existingDays = dailyWorkSchedulesRepository.findByMonthlySchedule_MonthlyScheduleId(monthlyId);

        java.util.Map<java.time.LocalDate, DailyWorkSchedules> existingDaysMap = existingDays.stream().collect(Collectors.toMap(DailyWorkSchedules::getWorkDate, day -> day));

        List<DailyWorkSchedules> toSaveList = new ArrayList<>();

        // 4. Xử lý mảng dữ liệu Frontend gửi xuống (Thêm mới hoặc Cập nhật)
        for (DailyScheduleDTO dto : incomingDays) {
            DailyWorkSchedules entity = existingDaysMap.get(dto.getWorkDate());

            Shift shift = null;
            if (dto.getShiftId() != null) {
                shift = shiftsRepository.findById(dto.getShiftId()).orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc: " + dto.getShiftId()));
            }

            if (entity != null) {
                // Đã có ngày này -> Cập nhật ca làm việc
                entity.setShift(shift);
                if (dto.getDayType() != null) {
                    entity.setDayType(DailyWorkSchedules.DayType.valueOf(dto.getDayType()));
                }
                toSaveList.add(entity);
                existingDaysMap.remove(dto.getWorkDate()); // Đánh dấu đã xử lý
            } else {
                // Chưa có ngày này -> Thêm mới
                DailyWorkSchedules newDay = new DailyWorkSchedules();
                newDay.setMonthlySchedule(monthEntity);
                newDay.setWorkDate(dto.getWorkDate());
                newDay.setShift(shift);
                if (dto.getDayType() != null) {
                    newDay.setDayType(DailyWorkSchedules.DayType.valueOf(dto.getDayType()));
                } else {
                    newDay.setDayType(DailyWorkSchedules.DayType.WORK); // Mặc định là ngày làm việc
                }
                toSaveList.add(newDay);
            }
        }

        // 5. Những ngày còn sót lại trong Map là do Trưởng phòng đã xóa trên web -> Xóa khỏi DB
        if (!existingDaysMap.isEmpty()) {
            dailyWorkSchedulesRepository.deleteAll(existingDaysMap.values());
        }

        // 6. Lưu tất cả (Insert & Update) xuống DB
        if (!toSaveList.isEmpty()) {
            dailyWorkSchedulesRepository.saveAll(toSaveList);
        }
    }

    public void updateScheduleStatus(Long monthlyScheduleId, String status) {
        MonthlySchedules monthEntity = monthlySchedulesRepository.findById(monthlyScheduleId).orElseThrow(() -> new RuntimeException("Không tìm thấy lịch tháng này!"));

        monthEntity.setStatus(MonthlySchedules.ScheduleStatus.valueOf(status));
        monthlySchedulesRepository.save(monthEntity);
        // CHÈN THÊM: Nếu chốt lịch (LOCKED) thì báo cho nhân viên
        if ("LOCKED".equals(status)) {
            notificationService.sendNotification(monthEntity.getEmployee().getUser().getId(), "📅 Lịch làm việc mới", "Lịch làm việc tháng " + monthEntity.getMonth() + " đã được chốt.", "INFO", "/my-schedule");
        }
    }
}
