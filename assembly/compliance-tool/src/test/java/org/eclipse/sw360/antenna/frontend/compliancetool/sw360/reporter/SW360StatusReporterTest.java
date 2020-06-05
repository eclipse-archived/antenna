package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SW360StatusReporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    SW360Configuration configurationMock = mock(SW360Configuration.class);

    private Set<String> parameter;

    @Test(expected = NullPointerException.class)
    public void testConfigurationMustNotBeNull() {
        new SW360StatusReporter(null, parameter);
    }

    @Test(expected = NullPointerException.class)
    public void testParameterMustNotBeNull() {
        new SW360StatusReporter(configurationMock, null);
    }

    @Test
    public void testStatusReporter() throws IOException {
        when(configurationMock.getTargetDir())
                .thenReturn(folder.getRoot().toPath());
        File csvFile = folder.newFile("result.csv");
        String csvFileName = csvFile.getName();
        when(configurationMock.getCsvFileName())
                .thenReturn(csvFileName);

        parameter = new HashSet<>(Collections.singletonList(new IRGetClearedReleases().getInfoParameter()));
        SW360StatusReporter statusReporter = new SW360StatusReporter(configurationMock, parameter);

        statusReporter.setInfoRequest(InfoRequest.emptyInfoRequest());

        statusReporter.execute();
        assertThat(csvFile).exists();
    }
}