package com.acme.hr.dao;

import com.acme.hr.model.dto.HeadcountRow;
import com.acme.hr.model.dto.PayStatsRow;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Dimension;
import com.acme.hr.util.Money;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Aggregations run in SQL so the app never loads 10k rows to compute a statistic. Only active
 * employees' current pay components are counted, and pay is always split by currency because
 * amounts in different currencies are never added together.
 */
@Repository
public class InsightsDao {

    private final JdbcClient jdbc;

    InsightsDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<HeadcountRow> headcount(Dimension dimension) {
        String sql = """
                SELECT %s AS grp,
                       SUM(CASE WHEN e.active = 1 THEN 1 ELSE 0 END) AS active,
                       SUM(CASE WHEN e.active = 1 THEN 0 ELSE 1 END) AS inactive
                FROM employee e
                GROUP BY grp
                ORDER BY active DESC, grp
                """.formatted(dimension.column());
        return jdbc.sql(sql)
                .query((rs, i) -> new HeadcountRow(rs.getString("grp"), rs.getLong("active"), rs.getLong("inactive")))
                .list();
    }

    /**
     * Monthly gross per employee (sum of current components), then min/median/avg/max/total per group.
     * The median uses window functions: the middle row, or the mean of the two middle rows.
     */
    public List<PayStatsRow> payStats(Dimension dimension, Country country) {
        String sql = """
                WITH employee_pay AS (
                    SELECT %s AS grp, s.currency AS currency, SUM(s.amount) AS pay
                    FROM employee e
                    JOIN salary_component s ON s.employee_id = e.id AND s.effective_to IS NULL
                    WHERE e.active = 1 AND (:country IS NULL OR e.country = :country)
                    GROUP BY e.id, s.currency
                ),
                ranked AS (
                    SELECT grp, currency, pay,
                           ROW_NUMBER() OVER (PARTITION BY grp, currency ORDER BY pay) AS rn,
                           COUNT(*) OVER (PARTITION BY grp, currency) AS cnt
                    FROM employee_pay
                )
                SELECT grp, currency,
                       COUNT(*) AS headcount,
                       MIN(pay) AS min_pay,
                       AVG(CASE WHEN rn IN ((cnt + 1) / 2, (cnt + 2) / 2) THEN pay END) AS median_pay,
                       AVG(pay) AS avg_pay,
                       MAX(pay) AS max_pay,
                       SUM(pay) AS total_pay
                FROM ranked
                GROUP BY grp, currency
                ORDER BY currency, total_pay DESC
                """.formatted(dimension.column());
        return jdbc.sql(sql)
                .param("country", country == null ? null : country.name())
                .query((rs, i) -> new PayStatsRow(
                        rs.getString("grp"), rs.getString("currency"), rs.getLong("headcount"),
                        money(rs, "min_pay"), money(rs, "median_pay"), money(rs, "avg_pay"),
                        money(rs, "max_pay"), money(rs, "total_pay")))
                .list();
    }

    private static BigDecimal money(ResultSet rs, String column) throws SQLException {
        return Money.round(rs.getBigDecimal(column));
    }
}
