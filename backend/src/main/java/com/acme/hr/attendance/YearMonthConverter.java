package com.acme.hr.attendance;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;

/** Stores months as sortable ISO text ("2026-11"). */
@Converter(autoApply = true)
class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth month) {
        return month == null ? null : month.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String value) {
        return value == null ? null : YearMonth.parse(value);
    }
}
