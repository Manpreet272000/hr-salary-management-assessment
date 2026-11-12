package com.acme.hr.service;

import com.acme.hr.dao.InsightsDao;
import com.acme.hr.model.dto.HeadcountRow;
import com.acme.hr.model.dto.PayStatsRow;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Dimension;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Answers "how does the org pay people?" from aggregate queries. */
@Service
@Transactional(readOnly = true)
public class InsightsService {

    private final InsightsDao insights;

    InsightsService(InsightsDao insights) {
        this.insights = insights;
    }

    public List<HeadcountRow> headcount(Dimension groupBy) {
        return insights.headcount(groupBy);
    }

    /** Monthly gross pay statistics per group and currency, optionally for one country. */
    public List<PayStatsRow> payStats(Dimension groupBy, Country country) {
        return insights.payStats(groupBy, country);
    }
}
