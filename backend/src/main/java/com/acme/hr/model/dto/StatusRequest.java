package com.acme.hr.model.dto;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull Boolean active) {
}
