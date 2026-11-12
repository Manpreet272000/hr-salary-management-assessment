package com.acme.hr.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmployeeCodeTest {

    @Test
    void formatsIdAsZeroPaddedCode() {
        assertThat(EmployeeCode.format(42)).isEqualTo("EMP000042");
    }

    @Test
    void parsesCodesCaseInsensitivelyWithOrWithoutPadding() {
        assertThat(EmployeeCode.parse("EMP000042")).contains(42L);
        assertThat(EmployeeCode.parse(" emp42 ")).contains(42L);
    }

    @Test
    void ignoresTextThatIsNotACode() {
        assertThat(EmployeeCode.parse("Emma")).isEmpty();
        assertThat(EmployeeCode.parse("EMP")).isEmpty();
        assertThat(EmployeeCode.parse(null)).isEmpty();
    }
}
