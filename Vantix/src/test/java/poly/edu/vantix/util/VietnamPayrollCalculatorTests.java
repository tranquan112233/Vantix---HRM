package poly.edu.vantix.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VietnamPayrollCalculatorTests {

    @Test
    void unpaidLeaveDaysReducePayableSalaryWhenActualDaysAreFull() {
        PayrollCalculation result = VietnamPayrollCalculator.calculate(PayrollInput.builder()
                .baseSalary(new BigDecimal("26000000"))
                .insuranceSalary(BigDecimal.ZERO)
                .standardWorkDays(26)
                .actualWorkDays(new BigDecimal("26"))
                .paidLeaveDays(BigDecimal.ZERO)
                .unpaidLeaveDays(new BigDecimal("1"))
                .build());

        assertEquals(new BigDecimal("25000000"), result.getWorkingDaysSalary());
    }

    @Test
    void unpaidLeaveDaysDoNotDoubleDeductWhenActualDaysAlreadyExcludeLeave() {
        PayrollCalculation result = VietnamPayrollCalculator.calculate(PayrollInput.builder()
                .baseSalary(new BigDecimal("26000000"))
                .insuranceSalary(BigDecimal.ZERO)
                .standardWorkDays(26)
                .actualWorkDays(new BigDecimal("25"))
                .paidLeaveDays(BigDecimal.ZERO)
                .unpaidLeaveDays(new BigDecimal("1"))
                .build());

        assertEquals(new BigDecimal("25000000"), result.getWorkingDaysSalary());
    }
}
