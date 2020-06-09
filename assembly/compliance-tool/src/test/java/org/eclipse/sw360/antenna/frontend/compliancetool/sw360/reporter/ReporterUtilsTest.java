package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterUtilsTest {

    @Test
    public void printCollectionOfReleasesTest() {
        final SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease("test");
        final Set<SW360SparseRelease> sparseReleases = Collections.singleton(sparseRelease);

        final String[] printedReleases = ReporterUtils.printCollectionOfSparseReleases(sparseReleases);

        assertThat(printedReleases.length).isEqualTo(sparseReleases.size());
        assertThat(printedReleases[0]).contains(sparseRelease.getName());
        assertThat(printedReleases[0]).contains(sparseRelease.getVersion());
        assertThat(printedReleases[0]).contains(sparseRelease.getReleaseId());
        final String delimiter = ";";
        assertThat(printedReleases[0]).contains(delimiter);

        final String spareseReleasesHeader = ReporterUtils.sparseReleaseCsvPrintHeader();
        assertThat(printedReleases[0].split(delimiter).length)
                .isEqualTo(spareseReleasesHeader.split(delimiter).length);
    }

    @Test
    public void printCollectionOfSparseReleasesTest() {
        final SW360Release release = SW360TestUtils.mkSW360Release("test");
        final Set<SW360Release> releases = Collections.singleton(release);

        final String[] printedReleases = ReporterUtils.printCollectionOfReleases(releases);

        assertThat(printedReleases.length).isEqualTo(releases.size());
        assertThat(printedReleases[0]).contains(release.getDownloadurl());
        assertThat(printedReleases[0]).contains(release.getName());
        assertThat(printedReleases[0]).contains(release.getCopyrights());
        final String delimiter = ";";
        assertThat(printedReleases[0]).contains(delimiter);

        final String releasesHeader = ReporterUtils.releaseCsvPrintHeader();
        assertThat(printedReleases[0].split(delimiter).length)
                .isEqualTo(releasesHeader.split(delimiter).length);

    }
}
