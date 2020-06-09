package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterOutputFactoryTest {
    @Test
    public void defaultStringEqualsDefaultTest() {
        final ReporterOutput reporterOutput = ReporterOutputFactory.getReporterOutput("");

        assertThat(reporterOutput).isEqualTo(ReporterOutputFactory.DEFAULT_REPORTER_OUTPUT);
    }
}
