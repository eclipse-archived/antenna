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

import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class BundleCoordinatesTest {

    @Test
    public void bundleCoordinatesTest() {
        BundleCoordinates coordinates = new BundleCoordinates();
        coordinates.setSymbolicName("testName");
        coordinates.setBundleVersion("version");
        assertThat(coordinates.equals("")).isFalse();
        BundleCoordinates coordinates2 = new BundleCoordinates();
        assertThat(coordinates2.equals(coordinates)).isFalse();
        assertThat(coordinates.equals(coordinates2)).isFalse();
        coordinates2.setSymbolicName("");
        assertThat(coordinates.equals(coordinates2)).isFalse();
        coordinates2.setBundleVersion("");
        assertThat(coordinates.equals(coordinates2)).isFalse();
        assertThat(coordinates.equals(coordinates)).isTrue();
        assertThat(coordinates.equals(null)).isFalse();
        assertThat(coordinates.getSymbolicName()).isEqualTo("testName");
        assertThat(coordinates.getBundleVersion()).isEqualTo("version");

    }

}
