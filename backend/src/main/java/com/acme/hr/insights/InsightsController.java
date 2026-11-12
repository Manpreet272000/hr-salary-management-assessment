package com.acme.hr.insights;

import com.acme.hr.common.Country;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Answers "how does the org pay people?" questions. */
@RestController
@RequestMapping("/api/insights")
class InsightsController {

    private final InsightsRepository insights;

    InsightsController(InsightsRepository insights) {
        this.insights = insights;
    }

    @GetMapping("/headcount")
    List<InsightsRepository.HeadcountRow> headcount(@RequestParam(defaultValue = "DEPARTMENT") Dimension groupBy) {
        return insights.headcount(groupBy);
    }

    /** Monthly gross pay statistics, e.g. {@code ?groupBy=DEPARTMENT&country=IN}. */
    @GetMapping("/pay")
    List<InsightsRepository.PayStatsRow> pay(@RequestParam(defaultValue = "DEPARTMENT") Dimension groupBy,
                                             @RequestParam(required = false) Country country) {
        return insights.payStats(groupBy, country);
    }
}
