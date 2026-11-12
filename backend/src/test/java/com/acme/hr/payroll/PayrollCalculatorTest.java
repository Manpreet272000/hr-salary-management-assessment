package com.acme.hr.payroll;

import static com.acme.hr.compensation.PayType.INCENTIVE;
import static com.acme.hr.compensation.PayType.REGULAR;
import static com.acme.hr.compensation.PayType.STIPEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.hr.attendance.LeaveSummary;
import com.acme.hr.compensation.PayType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayrollCalculatorTest {

    private static final int WORKING_DAYS = 20;

    private final PayrollCalculator calculator =
            new PayrollCalculator(new FlatRateTaxPolicy(new PayrollProperties(new BigDecimal("0.05"))));

    @Test
    void fullMonthWithoutLeaveIsGrossMinusFivePercentTax() {
        PayBreakdown pay = calculate(List.of(line(REGULAR, "100000")), LeaveSummary.NONE);

        assertThat(pay.grossPay()).isEqualByComparingTo("100000");
        assertThat(pay.lossOfPayDeduction()).isEqualByComparingTo("0");
        assertThat(pay.tax()).isEqualByComparingTo("5000");
        assertThat(pay.netPay()).isEqualByComparingTo("95000");
    }

    @Test
    void appliedSickAndCasualLeaveDoNotReducePay() {
        PayBreakdown pay = calculate(List.of(line(REGULAR, "100000")), new LeaveSummary(3, 2, 0));

        assertThat(pay.lossOfPayDays()).isZero();
        assertThat(pay.netPay()).isEqualByComparingTo("95000");
    }

    @Test
    void eachUnplannedLeaveDayDeductsOneWorkingDayOfFixedPayBeforeTax() {
        PayBreakdown pay = calculate(List.of(line(REGULAR, "100000")), new LeaveSummary(1, 1, 2));

        // 100000 × 2 / 20 = 10000 deducted; tax on the remaining 90000.
        assertThat(pay.lossOfPayDays()).isEqualTo(2);
        assertThat(pay.lossOfPayDeduction()).isEqualByComparingTo("10000");
        assertThat(pay.taxablePay()).isEqualByComparingTo("90000");
        assertThat(pay.tax()).isEqualByComparingTo("4500");
        assertThat(pay.netPay()).isEqualByComparingTo("85500");
    }

    @Test
    void incentiveIsPaidInFullRegardlessOfUnplannedLeave() {
        PayBreakdown pay = calculate(List.of(line(REGULAR, "60000"), line(INCENTIVE, "20000")), new LeaveSummary(0, 0, 5));

        assertThat(pay.fixedPay()).isEqualByComparingTo("60000");
        assertThat(pay.variablePay()).isEqualByComparingTo("20000");
        assertThat(pay.grossPay()).isEqualByComparingTo("80000");
        assertThat(pay.lossOfPayDeduction()).isEqualByComparingTo("15000"); // 60000 × 5 / 20
        assertThat(pay.netPay()).isEqualByComparingTo("61750");            // (80000 − 15000) × 0.95
    }

    @Test
    void stipendIsReducedByUnplannedLeaveLikeSalary() {
        PayBreakdown pay = calculate(List.of(line(STIPEND, "20000")), new LeaveSummary(0, 0, 4));

        assertThat(pay.lossOfPayDeduction()).isEqualByComparingTo("4000");
        assertThat(pay.netPay()).isEqualByComparingTo("15200");
    }

    @Test
    void absentEveryWorkingDayWithoutLeaveLeavesOnlyVariablePay() {
        PayBreakdown pay = calculate(List.of(line(REGULAR, "50000"), line(INCENTIVE, "1000")), new LeaveSummary(0, 0, WORKING_DAYS));

        assertThat(pay.taxablePay()).isEqualByComparingTo("1000");
        assertThat(pay.netPay()).isEqualByComparingTo("950");
    }

    @Test
    void deductionIsRoundedOnceToCents() {
        // 1000 × 1 / 21 = 47.619… → 47.62 (not 21 × a rounded per-day rate)
        PayBreakdown pay = calculator.calculate(List.of(line(REGULAR, "1000")), 21, new LeaveSummary(0, 0, 1));

        assertThat(pay.lossOfPayDeduction()).isEqualByComparingTo("47.62");
        assertThat(pay.tax()).isEqualByComparingTo("47.62");   // 952.38 × 0.05 = 47.619
        assertThat(pay.netPay()).isEqualByComparingTo("904.76");
    }

    @Test
    void noPayComponentsMeansZeroPayout() {
        PayBreakdown pay = calculate(List.of(), LeaveSummary.NONE);

        assertThat(pay.netPay()).isEqualByComparingTo("0");
    }

    @Test
    void rejectsMoreLossOfPayDaysThanWorkingDays() {
        assertThatThrownBy(() -> calculate(List.of(line(REGULAR, "1000")), new LeaveSummary(0, 0, WORKING_DAYS + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private PayBreakdown calculate(List<PayLine> lines, LeaveSummary leaves) {
        return calculator.calculate(lines, WORKING_DAYS, leaves);
    }

    private static PayLine line(PayType type, String amount) {
        return new PayLine(type, new BigDecimal(amount));
    }
}
