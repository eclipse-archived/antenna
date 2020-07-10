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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ExporterTest {

    @Test(expected = NullPointerException.class)
    public void testConfigurationMustNotBeNull() {
        new SW360Exporter(null);
    }

    @Test
    public void testReleasesComparator() {
        SW360Release release1 = new SW360Release();
        SW360Release release2 = new SW360Release();
        SW360Release release3 = new SW360Release();
        SW360Release release4 = new SW360Release();
        SW360Release release5 = new SW360Release();
        release1.setCreatedOn("2020-06-24")
                .setName("z-release")
                .setVersion("1.7.8");
        release2.setCreatedOn("2020-06-24")
                .setName("z-release")
                .setVersion("1.7.9");
        release3.setCreatedOn("2020-06-01")
                .setName("a-release")
                .setVersion("9.1");
        release4.setCreatedOn("2020-06-01")
                .setName("b-release")
                .setVersion("0.1-SNAPSHOT");
        release5.setCreatedOn("2020-06-01")
                .setName("b-release")
                .setVersion("0.2-SNAPSHOT");
        ReleaseWithSources releaseSrc1 = new ReleaseWithSources(release1,
                Collections.emptySet());
        ReleaseWithSources releaseSrc2 = new ReleaseWithSources(release2,
                Collections.emptySet());
        ReleaseWithSources releaseSrc3 = new ReleaseWithSources(release3,
                Collections.emptySet());
        ReleaseWithSources releaseSrc4 = new ReleaseWithSources(release4,
                Collections.emptySet());
        ReleaseWithSources releaseSrc5 = new ReleaseWithSources(release5,
                Collections.emptySet());
        List<ReleaseWithSources> releaseList = new ArrayList<>();
        releaseList.add(releaseSrc2);
        releaseList.add(releaseSrc5);
        releaseList.add(releaseSrc1);
        releaseList.add(releaseSrc4);
        releaseList.add(releaseSrc3);

        releaseList.sort(SW360Exporter.RELEASES_COMPARATOR);
        assertThat(releaseList)
                .containsExactly(releaseSrc1, releaseSrc2, releaseSrc3, releaseSrc4, releaseSrc5);
    }
}