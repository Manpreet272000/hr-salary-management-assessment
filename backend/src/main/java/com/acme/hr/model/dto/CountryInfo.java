package com.acme.hr.model.dto;

import com.acme.hr.model.enums.Country;

public record CountryInfo(Country code, String name, String currency) {
}
