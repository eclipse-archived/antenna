/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterUtilsTest {

    @Test
    public void printCollectionOfSparseReleasesTest() {
        final SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease("test");
        final Set<SW360SparseRelease> sparseReleases = Collections.singleton(sparseRelease);

        final String[][] printedReleases = ReporterUtils.printCollectionOfSparseReleases(sparseReleases);

        assertThat(printedReleases.length).isEqualTo(sparseReleases.size());
        assertThat(Arrays.asList(printedReleases[0]).contains(sparseRelease.getName())).isTrue();
        assertThat(Arrays.asList(printedReleases[0]).contains(sparseRelease.getVersion())).isTrue();
        assertThat(Arrays.asList(printedReleases[0]).contains(sparseRelease.getReleaseId())).isTrue();

        final String delimiter = ";";
        final String sparseReleasesHeader = ReporterUtils.sparseReleaseCsvPrintHeader(delimiter);
        assertThat(printedReleases[0].length)
                .isEqualTo(sparseReleasesHeader.split(delimiter).length);
    }

    @Test
    public void printCollectionOfReleasesTest() {
        final SW360Release release = SW360TestUtils.mkSW360Release("test");
        final Set<SW360Release> releases = Collections.singleton(release);

        final String[][] printedReleases = ReporterUtils.printCollectionOfReleases(releases);

        assertThat(printedReleases.length).isEqualTo(releases.size());
        assertThat(Arrays.asList(printedReleases[0]).contains(release.getDownloadurl())).isTrue();
        assertThat(Arrays.asList(printedReleases[0]).contains(release.getName())).isTrue();
        assertThat(Arrays.asList(printedReleases[0]).contains(release.getCopyrights())).isTrue();

        final String delimiter = ";";
        final String releasesHeader = ReporterUtils.releaseCsvPrintHeader(delimiter);
        assertThat(printedReleases[0].length)
                .isEqualTo(releasesHeader.split(delimiter).length);

    }
}
