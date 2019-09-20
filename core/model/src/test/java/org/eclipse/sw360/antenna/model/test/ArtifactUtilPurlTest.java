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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.util.ArtifactUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ArtifactUtilPurlTest {
    private static final String MAVENPURL = "pkg:maven/com.something/foo@1.2.3";
    private static final String P2PURL = "pkg:p2/com.something.foo@1.2.3";
    private static final String NUGETPURL = "pkg:nuget/foo-dll@1.2.3";
    private static final String JSNAMESPACEPURL = "pkg:npm/%40angular/animation@1.2.3";
    private static final String JSSIMPLEPURL = "pkg:npm/foobar@12.3.1";

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { MAVENPURL, MavenCoordinates.class },
                { P2PURL, BundleCoordinates.class },
                { NUGETPURL, DotNetCoordinates.class },
                { JSNAMESPACEPURL, JavaScriptCoordinates.class },
                { JSSIMPLEPURL, JavaScriptCoordinates.class }
        });
    }

    private String inputPurl;
    private Class<ArtifactCoordinates> expectedCoordinateClass;

    public ArtifactUtilPurlTest(String inputPurl, Class<ArtifactCoordinates> expectedCoordinateClass) {
        this.inputPurl = inputPurl;
        this.expectedCoordinateClass = expectedCoordinateClass;
    }

    @Test
    public void testPurlRoundtrip() {
        ArtifactCoordinates coordinates = ArtifactUtils.createArtifactCoordinatesFromPurl(inputPurl);

        assertThat(coordinates).isInstanceOf(expectedCoordinateClass);
        assertThat(coordinates.getPurl().canonicalize()).isEqualTo(inputPurl);
    }
}
