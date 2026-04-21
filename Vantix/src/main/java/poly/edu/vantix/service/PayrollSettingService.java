package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.PayrollSettingRequest;
import poly.edu.vantix.dto.response.PayrollSettingResponse;
import poly.edu.vantix.entity.PayrollSetting;
import poly.edu.vantix.repository.PayrollSettingRepository;
import poly.edu.vantix.util.PayrollPolicy;

@Service
public class PayrollSettingService {

    private static final String DEFAULT_KEY = "DEFAULT";

    private final PayrollSettingRepository payrollSettingRepository;

    public PayrollSettingService(PayrollSettingRepository payrollSettingRepository) {
        this.payrollSettingRepository = payrollSettingRepository;
    }

    @Transactional
    public PayrollSettingResponse get() {
        return PayrollSettingResponse.fromEntity(getOrCreate());
    }

    @Transactional
    public PayrollPolicy getPolicy() {
        return getOrCreate().toPolicy();
    }

    @Transactional
    public PayrollSettingResponse update(PayrollSettingRequest request) {
        PayrollSetting setting = getOrCreate();
        setting.setEmployeeSocialInsuranceRate(request.getEmployeeSocialInsuranceRate());
        setting.setEmployeeHealthInsuranceRate(request.getEmployeeHealthInsuranceRate());
        setting.setEmployeeUnemploymentInsuranceRate(request.getEmployeeUnemploymentInsuranceRate());
        setting.setEmployerSocialInsuranceRate(request.getEmployerSocialInsuranceRate());
        setting.setEmployerHealthInsuranceRate(request.getEmployerHealthInsuranceRate());
        setting.setEmployerUnemploymentInsuranceRate(request.getEmployerUnemploymentInsuranceRate());
        setting.setGovernmentBaseSalary(request.getGovernmentBaseSalary());
        setting.setMinRegionalSalary(request.getMinRegionalSalary());
        setting.setPersonalDeduction(request.getPersonalDeduction());
        setting.setDependentDeduction(request.getDependentDeduction());
        setting.setMealAllowanceExempt(request.getMealAllowanceExempt());
        setting.setOvertimeWeekdayMultiplier(request.getOvertimeWeekdayMultiplier());
        setting.setOvertimeWeekendMultiplier(request.getOvertimeWeekendMultiplier());
        setting.setOvertimeHolidayMultiplier(request.getOvertimeHolidayMultiplier());
        setting.setOvertimeNightMultiplier(request.getOvertimeNightMultiplier());
        return PayrollSettingResponse.fromEntity(payrollSettingRepository.save(setting));
    }

    @Transactional
    public PayrollSettingResponse reset() {
        PayrollSetting setting = getOrCreate();
        PayrollSetting defaults = new PayrollSetting();
        setting.setEmployeeSocialInsuranceRate(defaults.getEmployeeSocialInsuranceRate());
        setting.setEmployeeHealthInsuranceRate(defaults.getEmployeeHealthInsuranceRate());
        setting.setEmployeeUnemploymentInsuranceRate(defaults.getEmployeeUnemploymentInsuranceRate());
        setting.setEmployerSocialInsuranceRate(defaults.getEmployerSocialInsuranceRate());
        setting.setEmployerHealthInsuranceRate(defaults.getEmployerHealthInsuranceRate());
        setting.setEmployerUnemploymentInsuranceRate(defaults.getEmployerUnemploymentInsuranceRate());
        setting.setGovernmentBaseSalary(defaults.getGovernmentBaseSalary());
        setting.setMinRegionalSalary(defaults.getMinRegionalSalary());
        setting.setPersonalDeduction(defaults.getPersonalDeduction());
        setting.setDependentDeduction(defaults.getDependentDeduction());
        setting.setMealAllowanceExempt(defaults.getMealAllowanceExempt());
        setting.setOvertimeWeekdayMultiplier(defaults.getOvertimeWeekdayMultiplier());
        setting.setOvertimeWeekendMultiplier(defaults.getOvertimeWeekendMultiplier());
        setting.setOvertimeHolidayMultiplier(defaults.getOvertimeHolidayMultiplier());
        setting.setOvertimeNightMultiplier(defaults.getOvertimeNightMultiplier());
        return PayrollSettingResponse.fromEntity(payrollSettingRepository.save(setting));
    }

    private PayrollSetting getOrCreate() {
        return payrollSettingRepository.findBySettingKey(DEFAULT_KEY)
                .orElseGet(() -> {
                    PayrollSetting setting = new PayrollSetting();
                    setting.setSettingKey(DEFAULT_KEY);
                    return payrollSettingRepository.save(setting);
                });
    }
}
