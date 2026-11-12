package com.acme.hr.model.dto;

import com.acme.hr.model.enums.LeaveType;

public record LeaveTypeInfo(LeaveType code, boolean paid) {
}
