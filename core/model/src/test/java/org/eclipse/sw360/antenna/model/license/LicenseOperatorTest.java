package org.eclipse.sw360.antenna.model.license;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseOperatorTest {
    @Test
    public void testLicenseOperator() {
        assertThat(LicenseOperator.fromValue("and"))
                .isEqualTo(LicenseOperator.AND);
        assertThat(LicenseOperator.fromValue("And"))
                .isEqualTo(LicenseOperator.AND);
        assertThat(LicenseOperator.fromValue("AND"))
                .isEqualTo(LicenseOperator.AND);

        assertThat(LicenseOperator.fromValue("or"))
                .isEqualTo(LicenseOperator.OR);
        assertThat(LicenseOperator.fromValue("Or"))
                .isEqualTo(LicenseOperator.OR);
        assertThat(LicenseOperator.fromValue("OR"))
                .isEqualTo(LicenseOperator.OR);
    }
}
