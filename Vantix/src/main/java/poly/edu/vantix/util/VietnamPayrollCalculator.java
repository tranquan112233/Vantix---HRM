package poly.edu.vantix.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/*
 * VietnamPayrollCalculator
 * - Tính lương Net và các khoản khấu trừ theo pháp luật Việt Nam hiện hành
 *
 * Căn cứ pháp lý:
 *   - Luật BHXH 2014 và Nghị định 58/2020, 143/2018
 *   - Luật Thuế TNCN + Nghị quyết 954/2020/UBTVQH14 (giảm trừ)
 *   - Thông tư 111/2013/TT-BTC (biểu thuế lũy tiến)
 *   - Quyết định 595/QĐ-BHXH
 *
 * Tỷ lệ đóng của NGƯỜI LAO ĐỘNG (tổng 10.5%):
 *   - BHXH:  8%
 *   - BHYT:  1.5%
 *   - BHTN:  1%
 * Tỷ lệ đóng của NGƯỜI SỬ DỤNG LAO ĐỘNG (tổng 21.5%):
 *   - BHXH:  17.5% (bao gồm 14% hưu trí, 3% ốm đau-thai sản, 0.5% TNLĐ-BNN)
 *   - BHYT:  3%
 *   - BHTN:  1%
 *
 * Mức trần lương đóng BH:
 *   - BHXH + BHYT: tối đa 20 lần mức lương cơ sở (2.34M × 20 = 46.8M từ 01/07/2024)
 *   - BHTN: tối đa 20 lần mức lương tối thiểu vùng
 *
 * Giảm trừ gia cảnh:
 *   - Bản thân: 11.000.000đ/tháng
 *   - Mỗi người phụ thuộc: 4.400.000đ/tháng
 *
 * Biểu thuế TNCN lũy tiến (phần từng phần thu nhập chịu thuế/tháng):
 *   Đến 5 triệu              5%
 *   Trên  5 đến  10 triệu   10%
 *   Trên 10 đến  18 triệu   15%
 *   Trên 18 đến  32 triệu   20%
 *   Trên 32 đến  52 triệu   25%
 *   Trên 52 đến  80 triệu   30%
 *   Trên 80 triệu           35%
 */
public final class VietnamPayrollCalculator {

    // Tỷ lệ trích của người lao động
    public static final BigDecimal RATE_EMPLOYEE_SI = new BigDecimal("0.08");   // BHXH
    public static final BigDecimal RATE_EMPLOYEE_HI = new BigDecimal("0.015");  // BHYT
    public static final BigDecimal RATE_EMPLOYEE_UI = new BigDecimal("0.01");   // BHTN

    // Tỷ lệ trích của người sử dụng lao động
    public static final BigDecimal RATE_EMPLOYER_SI = new BigDecimal("0.175");
    public static final BigDecimal RATE_EMPLOYER_HI = new BigDecimal("0.03");
    public static final BigDecimal RATE_EMPLOYER_UI = new BigDecimal("0.01");

    // Mức lương cơ sở (tham chiếu cho trần BHXH/BHYT) - 2.340.000 từ 01/07/2024
    public static final BigDecimal BASE_SALARY_GOV = new BigDecimal("2340000");

    // Trần đóng BHXH/BHYT = 20 lần lương cơ sở
    public static final BigDecimal MAX_SI_HI_SALARY = BASE_SALARY_GOV.multiply(BigDecimal.valueOf(20));

    // Mức lương tối thiểu vùng I (tham chiếu cho trần BHTN)
    public static final BigDecimal MIN_REGIONAL_SALARY = new BigDecimal("4960000");

    // Trần BHTN = 20 lần lương tối thiểu vùng
    public static final BigDecimal MAX_UI_SALARY = MIN_REGIONAL_SALARY.multiply(BigDecimal.valueOf(20));

    // Giảm trừ gia cảnh
    public static final BigDecimal PERSONAL_DEDUCTION = new BigDecimal("11000000");
    public static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("4400000");

    // Mức miễn thuế tiền ăn trưa (730.000đ/tháng theo Thông tư 26/2016/TT-BLĐTBXH)
    public static final BigDecimal MAX_MEAL_ALLOWANCE_EXEMPT = new BigDecimal("730000");

    // Các bậc thuế lũy tiến TNCN (tháng)
    private static final TaxBracket[] PIT_BRACKETS = new TaxBracket[] {
            new TaxBracket(new BigDecimal("5000000"),  new BigDecimal("0.05")),
            new TaxBracket(new BigDecimal("10000000"), new BigDecimal("0.10")),
            new TaxBracket(new BigDecimal("18000000"), new BigDecimal("0.15")),
            new TaxBracket(new BigDecimal("32000000"), new BigDecimal("0.20")),
            new TaxBracket(new BigDecimal("52000000"), new BigDecimal("0.25")),
            new TaxBracket(new BigDecimal("80000000"), new BigDecimal("0.30")),
            new TaxBracket(null,                       new BigDecimal("0.35")),
    };

    private VietnamPayrollCalculator() {}

    /**
     * Tính BHXH cá nhân phải đóng (có áp trần 20 lần lương cơ sở).
     */
    public static BigDecimal socialInsurance(BigDecimal insuranceSalary) {
        return cappedSalary(insuranceSalary, MAX_SI_HI_SALARY).multiply(RATE_EMPLOYEE_SI);
    }

    /**
     * Tính BHYT cá nhân phải đóng.
     */
    public static BigDecimal healthInsurance(BigDecimal insuranceSalary) {
        return cappedSalary(insuranceSalary, MAX_SI_HI_SALARY).multiply(RATE_EMPLOYEE_HI);
    }

    /**
     * Tính BHTN cá nhân phải đóng (trần 20 lần lương tối thiểu vùng).
     */
    public static BigDecimal unemploymentInsurance(BigDecimal insuranceSalary) {
        return cappedSalary(insuranceSalary, MAX_UI_SALARY).multiply(RATE_EMPLOYEE_UI);
    }

    /**
     * Tính tổng các khoản bảo hiểm người lao động đóng = BHXH + BHYT + BHTN.
     */
    public static BigDecimal totalEmployeeInsurance(BigDecimal insuranceSalary) {
        return socialInsurance(insuranceSalary)
                .add(healthInsurance(insuranceSalary))
                .add(unemploymentInsurance(insuranceSalary))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính tổng bảo hiểm người sử dụng lao động đóng.
     */
    public static BigDecimal totalEmployerInsurance(BigDecimal insuranceSalary) {
        BigDecimal siHi = cappedSalary(insuranceSalary, MAX_SI_HI_SALARY);
        BigDecimal ui   = cappedSalary(insuranceSalary, MAX_UI_SALARY);
        return siHi.multiply(RATE_EMPLOYER_SI)
                .add(siHi.multiply(RATE_EMPLOYER_HI))
                .add(ui.multiply(RATE_EMPLOYER_UI))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính thuế TNCN theo biểu lũy tiến từng phần.
     * @param taxableIncome thu nhập tính thuế tháng (đã trừ BH, giảm trừ gia cảnh, miễn thuế)
     */
    public static BigDecimal personalIncomeTax(BigDecimal taxableIncome) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remaining = taxableIncome;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal lowerBound = BigDecimal.ZERO;

        for (TaxBracket bracket : PIT_BRACKETS) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal bracketWidth;
            if (bracket.upperBound == null) {
                bracketWidth = remaining;
            } else {
                bracketWidth = bracket.upperBound.subtract(lowerBound);
                lowerBound = bracket.upperBound;
            }

            BigDecimal portion = remaining.min(bracketWidth);
            tax = tax.add(portion.multiply(bracket.rate));
            remaining = remaining.subtract(portion);
        }

        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính trọn gói payroll cho một nhân viên trong tháng.
     */
    public static PayrollCalculation calculate(PayrollInput input) {
        PayrollPolicy policy = input.getPolicy() != null ? input.getPolicy() : PayrollPolicy.defaults();
        BigDecimal base = nz(input.getBaseSalary());
        BigDecimal standardDays = new BigDecimal(Math.max(input.getStandardWorkDays(), 1));
        BigDecimal actualDays = nz(input.getActualWorkDays());
        BigDecimal paidLeaveDays = nz(input.getPaidLeaveDays());
        BigDecimal unpaidLeaveDays = nz(input.getUnpaidLeaveDays());

        // Lương theo ngày công thực tế + ngày nghỉ có phép
        BigDecimal payableDays = actualDays.add(paidLeaveDays)
                .min(standardDays.subtract(unpaidLeaveDays).max(BigDecimal.ZERO));
        BigDecimal salaryByDays = base
                .multiply(payableDays)
                .divide(standardDays, 2, RoundingMode.HALF_UP);

        // Tiền tăng ca
        // Ngày thường 150%, cuối tuần 200%, lễ/tết 300%, đêm +30%
        BigDecimal dailyRate = base.divide(standardDays, 6, RoundingMode.HALF_UP);
        BigDecimal hourlyRate = dailyRate.divide(nz2(input.getHoursPerDay(), new BigDecimal("8")), 6, RoundingMode.HALF_UP);

        BigDecimal otWeekday = nz(input.getOvertimeHoursWeekday()).multiply(hourlyRate).multiply(nz(policy.getOvertimeWeekdayMultiplier()));
        BigDecimal otWeekend = nz(input.getOvertimeHoursWeekend()).multiply(hourlyRate).multiply(nz(policy.getOvertimeWeekendMultiplier()));
        BigDecimal otHoliday = nz(input.getOvertimeHoursHoliday()).multiply(hourlyRate).multiply(nz(policy.getOvertimeHolidayMultiplier()));
        BigDecimal otNight = nz(input.getOvertimeHoursNight()).multiply(hourlyRate).multiply(nz(policy.getOvertimeNightMultiplier()));
        BigDecimal totalOvertime = otWeekday.add(otWeekend).add(otHoliday).add(otNight)
                .setScale(0, RoundingMode.HALF_UP);

        // Tổng phụ cấp
        BigDecimal allowanceTaxable = nz(input.getResponsibilityAllowance())
                .add(nz(input.getTransportAllowance()))
                .add(nz(input.getOtherAllowance()));

        BigDecimal mealAllowance = nz(input.getMealAllowance());
        BigDecimal mealExempt = mealAllowance.min(nz(policy.getMealAllowanceExempt()));
        BigDecimal mealTaxable = mealAllowance.subtract(mealExempt).max(BigDecimal.ZERO);

        BigDecimal phoneAllowance = nz(input.getPhoneAllowance()); // Miễn thuế theo quy chế công ty
        BigDecimal totalAllowance = allowanceTaxable.add(mealAllowance).add(phoneAllowance);

        // Thưởng / hoa hồng / lương KPI
        BigDecimal bonus = nz(input.getBonus());
        BigDecimal commission = nz(input.getCommission());

        // Phạt / khấu trừ khác
        BigDecimal deductions = nz(input.getOtherDeductions());

        // Tổng thu nhập (gross)
        BigDecimal grossIncome = salaryByDays
                .add(totalOvertime)
                .add(totalAllowance)
                .add(bonus)
                .add(commission)
                .setScale(0, RoundingMode.HALF_UP);

        // Lương đóng BH: ưu tiên insuranceSalary, không có thì lấy base
        BigDecimal insuranceSalary = input.getInsuranceSalary() == null
                ? base
                : input.getInsuranceSalary();

        BigDecimal si = socialInsurance(insuranceSalary, policy).setScale(0, RoundingMode.HALF_UP);
        BigDecimal hi = healthInsurance(insuranceSalary, policy).setScale(0, RoundingMode.HALF_UP);
        BigDecimal ui = unemploymentInsurance(insuranceSalary, policy).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalInsurance = si.add(hi).add(ui);

        // Thu nhập chịu thuế = gross - các khoản miễn thuế (meal exempt, phone, tiền tăng ca phần vượt giờ thường được miễn nhưng để đơn giản tính vào)
        BigDecimal nonTaxableIncome = mealExempt.add(phoneAllowance);
        BigDecimal taxableBeforeDeduction = grossIncome.subtract(nonTaxableIncome).max(BigDecimal.ZERO);

        // Giảm trừ bản thân + người phụ thuộc + bảo hiểm
        int dependents = input.getDependents() == null ? 0 : Math.max(input.getDependents(), 0);
        BigDecimal personalDeduction = nz(policy.getPersonalDeduction());
        BigDecimal dependentDeduction = nz(policy.getDependentDeduction()).multiply(new BigDecimal(dependents));
        BigDecimal totalDeduction = personalDeduction.add(dependentDeduction).add(totalInsurance);

        BigDecimal taxableIncome = taxableBeforeDeduction.subtract(totalDeduction).max(BigDecimal.ZERO);
        BigDecimal personalIncomeTax = personalIncomeTax(taxableIncome);

        // Lương thực nhận
        BigDecimal netIncome = grossIncome
                .subtract(totalInsurance)
                .subtract(personalIncomeTax)
                .subtract(deductions)
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal employerInsurance = totalEmployerInsurance(insuranceSalary, policy);
        BigDecimal employerCost = grossIncome.add(employerInsurance).setScale(0, RoundingMode.HALF_UP);

        return PayrollCalculation.builder()
                .baseSalary(base)
                .insuranceSalary(insuranceSalary)
                .workingDaysSalary(salaryByDays.setScale(0, RoundingMode.HALF_UP))
                .overtimePay(totalOvertime)
                .allowance(totalAllowance)
                .mealAllowanceExempt(mealExempt)
                .mealAllowanceTaxable(mealTaxable)
                .bonus(bonus)
                .commission(commission)
                .grossIncome(grossIncome)
                .socialInsurance(si)
                .healthInsurance(hi)
                .unemploymentInsurance(ui)
                .totalEmployeeInsurance(totalInsurance)
                .personalDeduction(personalDeduction)
                .dependentDeduction(dependentDeduction)
                .taxableIncome(taxableIncome)
                .personalIncomeTax(personalIncomeTax)
                .otherDeductions(deductions)
                .netIncome(netIncome)
                .employerInsurance(employerInsurance)
                .totalEmployerCost(employerCost)
                .build();
    }

    private static BigDecimal socialInsurance(BigDecimal insuranceSalary, PayrollPolicy policy) {
        return cappedSalary(insuranceSalary, nz(policy.getGovernmentBaseSalary()).multiply(BigDecimal.valueOf(20)))
                .multiply(nz(policy.getEmployeeSocialInsuranceRate()));
    }

    private static BigDecimal healthInsurance(BigDecimal insuranceSalary, PayrollPolicy policy) {
        return cappedSalary(insuranceSalary, nz(policy.getGovernmentBaseSalary()).multiply(BigDecimal.valueOf(20)))
                .multiply(nz(policy.getEmployeeHealthInsuranceRate()));
    }

    private static BigDecimal unemploymentInsurance(BigDecimal insuranceSalary, PayrollPolicy policy) {
        return cappedSalary(insuranceSalary, nz(policy.getMinRegionalSalary()).multiply(BigDecimal.valueOf(20)))
                .multiply(nz(policy.getEmployeeUnemploymentInsuranceRate()));
    }

    private static BigDecimal totalEmployerInsurance(BigDecimal insuranceSalary, PayrollPolicy policy) {
        BigDecimal siHi = cappedSalary(insuranceSalary, nz(policy.getGovernmentBaseSalary()).multiply(BigDecimal.valueOf(20)));
        BigDecimal ui = cappedSalary(insuranceSalary, nz(policy.getMinRegionalSalary()).multiply(BigDecimal.valueOf(20)));
        return siHi.multiply(nz(policy.getEmployerSocialInsuranceRate()))
                .add(siHi.multiply(nz(policy.getEmployerHealthInsuranceRate())))
                .add(ui.multiply(nz(policy.getEmployerUnemploymentInsuranceRate())))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal cappedSalary(BigDecimal salary, BigDecimal cap) {
        if (salary == null) {
            return BigDecimal.ZERO;
        }
        return salary.min(cap).max(BigDecimal.ZERO);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal nz2(BigDecimal v, BigDecimal fallback) {
        return v == null || v.compareTo(BigDecimal.ZERO) <= 0 ? fallback : v;
    }

    public static List<String> bracketsDescription() {
        List<String> out = new ArrayList<>();
        out.add("Bậc 1: đến 5.000.000đ - 5%");
        out.add("Bậc 2: trên 5 đến 10.000.000đ - 10%");
        out.add("Bậc 3: trên 10 đến 18.000.000đ - 15%");
        out.add("Bậc 4: trên 18 đến 32.000.000đ - 20%");
        out.add("Bậc 5: trên 32 đến 52.000.000đ - 25%");
        out.add("Bậc 6: trên 52 đến 80.000.000đ - 30%");
        out.add("Bậc 7: trên 80.000.000đ - 35%");
        return out;
    }

    private record TaxBracket(BigDecimal upperBound, BigDecimal rate) {}
}
