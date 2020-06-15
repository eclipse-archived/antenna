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
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class IRGetClearedReleasesTest {

    @Test
    public void executeGetClearedReleasesTest() {
        SW360Release release = SW360TestUtils.mkSW360Release("test");
        release.setClearingState("PROJECT_APPROVED");
        release.setSw360ClearingState(SW360ClearingState.REPORT_AVAILABLE);
        SW360Connection connection = IRForReleasesHelper.getSW360Connection(release);

        final IRGetClearedReleases getClearedReleases = new IRGetClearedReleases();
        final Collection<SW360Release> clearedReleases = getClearedReleases.execute(connection);

        assertThat(clearedReleases).containsExactly(release);
    }

    @Test
    public void getTypeTest() {
        assertThat(new IRGetClearedReleases().getType()).isEqualTo(SW360Release.class);
    }

    @Test
    public void helpMessageTest() {
        final IRGetClearedReleases irGetNotClearedReleases = new IRGetClearedReleases();
        assertThat(irGetNotClearedReleases.helpMessage()).contains(irGetNotClearedReleases.getInfoParameter());
    }
}