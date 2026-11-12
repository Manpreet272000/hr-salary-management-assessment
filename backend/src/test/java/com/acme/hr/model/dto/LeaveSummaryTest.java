package com.acme.hr.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.hr.model.enums.LeaveType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LeaveSummaryTest {

    @Test
    void onlyUnpaidLeaveTypesCountAsLossOfPay() {
        LeaveSummary leave = new LeaveSummary(Map.of(LeaveType.SICK, 2, LeaveType.CASUAL, 1, LeaveType.UNPLANNED, 3));

        assertThat(leave.totalDays()).isEqualTo(6);
        assertThat(leave.lossOfPayDays()).isEqualTo(3);
    }

    @Test
    void missingLeaveTypesCountAsZero() {
        LeaveSummary leave = new LeaveSummary(Map.of(LeaveType.SICK, 2));

        assertThat(leave.daysOf(LeaveType.UNPLANNED)).isZero();
        assertThat(leave.days()).containsOnlyKeys(LeaveType.values());
        assertThat(LeaveSummary.NONE.totalDays()).isZero();
    }

    @Test
    void readsAndWritesJsonAsAPlainObjectKeyedByLeaveType() throws Exception {
        ObjectMapper json = new ObjectMapper();

        LeaveSummary leave = json.readValue("{\"CASUAL\": 2}", LeaveSummary.class);

        assertThat(leave.daysOf(LeaveType.CASUAL)).isEqualTo(2);
        assertThat(json.writeValueAsString(leave)).isEqualTo("{\"SICK\":0,\"CASUAL\":2,\"UNPLANNED\":0}");
    }
}
