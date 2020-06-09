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
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class IRGetNotClearedReleasesTest {

    @Test
    public void executeGetNotClearedReleasesTest() {
        SW360Release release = SW360TestUtils.mkSW360Release("test");
        release.setClearingState("INITIAL");
        SW360Connection connection = IRGetClearedReleasesTest.getSW360Connection(release);

        final IRGetNotClearedReleases getNotClearedReleases = new IRGetNotClearedReleases();
        final Collection<SW360Release> notClearedReleases = getNotClearedReleases.execute(connection);

        assertThat(notClearedReleases).containsExactly(release);
    }

    @Test
    public void getTypeTest() {
        assertThat(new IRGetNotClearedReleases().getType()).isEqualTo(SW360Release.class);
    }

    @Test
    public void helpMessageTest() {
        final IRGetNotClearedReleases irGetNotClearedReleases = new IRGetNotClearedReleases();
        assertThat(irGetNotClearedReleases.helpMessage()).contains(irGetNotClearedReleases.getInfoParameter());
    }
}