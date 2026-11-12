package com.acme.hr.controller;

import com.acme.hr.model.dto.HeadcountRow;
import com.acme.hr.model.dto.PayStatsRow;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Dimension;
import com.acme.hr.service.InsightsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Answers "how does the org pay people?" questions. */
@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insights;

    InsightsController(InsightsService insights) {
        this.insights = insights;
    }

    @GetMapping("/headcount")
    List<HeadcountRow> headcount(@RequestParam(defaultValue = "DEPARTMENT") Dimension groupBy) {
        return insights.headcount(groupBy);
    }

    /** Monthly gross pay statistics, e.g. {@code ?groupBy=DEPARTMENT&country=IN}. */
    @GetMapping("/pay")
    List<PayStatsRow> pay(@RequestParam(defaultValue = "DEPARTMENT") Dimension groupBy,
                                             @RequestParam(required = false) Country country) {
        return insights.payStats(groupBy, country);
    }
}
