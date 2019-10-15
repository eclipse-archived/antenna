/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.coordinates;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoordinateTest {
    @Test
    public void equalsTestFromString() {
        String purlString = "pkg:maven/test.groupId/artifactId@1.0.0";
        Coordinate coordinate1 = new Coordinate(purlString);
        Coordinate coordinate2 = new Coordinate(purlString);
        assertThat(coordinate1)
                .isEqualTo(coordinate2);
    }

    @Test
    public void matchesTestFromString() {
        String purlString = "pkg:maven/test.groupId/artifactId@1.0.0";
        String purlWildcardString = "pkg:maven/test.*/artifactId@1.0.0";
        Coordinate coordinate = new Coordinate(purlString);
        Coordinate coordinateWildcard = new Coordinate(purlWildcardString);
        assertThat(coordinateWildcard.matches(coordinate))
                .isTrue();
        assertThat(coordinate.matches(coordinateWildcard))
                .isFalse();
    }
}
