/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MavenCoordinatesTest {

    private MavenCoordinates coordinates;

    @Before
    public void init() {
        this.coordinates = new MavenCoordinates();
        this.coordinates.setArtifactId("testId");
        this.coordinates.setGroupId("testGroupId");
        this.coordinates.setVersion("testVersion");
    }

    @Test
    public void idTest() {
        assertThat(coordinates.getArtifactId()).isEqualTo("testId");
    }

    @Test
    public void groupIdTest() {
        assertThat(coordinates.getGroupId()).isEqualTo("testGroupId");
    }

    @Test
    public void versionTest() {
        assertThat(coordinates.getVersion()).isEqualTo("testVersion");
    }
}
