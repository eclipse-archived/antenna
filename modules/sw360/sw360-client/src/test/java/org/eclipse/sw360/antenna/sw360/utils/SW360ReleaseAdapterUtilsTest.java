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
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ReleaseAdapterUtilsTest {

    @Test
    public void isValidReleaseWithValidRelease() {
        SW360Release release = new SW360Release()
                .setName("releaseName")
                .setVersion("1.0-SNAPSHOT");

        boolean validRelease = SW360ReleaseAdapterUtils.isValidRelease(release);

        assertThat(validRelease).isTrue();
    }

    @Test
    public void isValidReleaseWithInvalidRelease() {
        SW360Release release = new SW360Release();

        boolean validRelease = SW360ReleaseAdapterUtils.isValidRelease(release);

        assertThat(validRelease).isFalse();
    }
}