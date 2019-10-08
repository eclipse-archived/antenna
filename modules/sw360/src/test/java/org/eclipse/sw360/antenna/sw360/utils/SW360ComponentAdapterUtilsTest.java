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

package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SW360ComponentAdapterUtilsTest {
    private static final ArtifactCoordinates MVN = new MavenCoordinates("artifact.id", "any.group.id", "1.0.0");
    private static final ArtifactCoordinates P2 = new BundleCoordinates("anySymbolicName", "1.0.0");
    private static final ArtifactCoordinates JS_WITHOUT_NS = new JavaScriptCoordinates("", "anyPackage", "1.4");
    private static final ArtifactCoordinates JS_WITH_NS = new JavaScriptCoordinates("@anyNamespace", "anyPackage", "1.4");
    private static final ArtifactCoordinates NUGET = new DotNetCoordinates("anyDll", "2.0.0");

    @Parameters
    public static Collection<Object[]> coordinateToName() {
        return Arrays.asList(new Object[][]{
                { MVN, "any.group.id:artifact.id" },
                { P2, "anySymbolicName" },
                { JS_WITHOUT_NS, "anyPackage" },
                { JS_WITH_NS, "@anyNamespace/anyPackage" },
                { NUGET, "anyDll" }
        });
    }

    private ArtifactCoordinates inputCoordinates;
    private String expectedComponentName;

    public SW360ComponentAdapterUtilsTest(ArtifactCoordinates inputCoordinates, String expectedComponentName) {
        this.inputCoordinates = inputCoordinates;
        this.expectedComponentName = expectedComponentName;
    }

    @Test
    public void testCreateComponentName() {
        Artifact artifact = new Artifact()
                .addFact(inputCoordinates);

        String componentName = SW360ComponentAdapterUtils.createComponentName(artifact);

        assertThat(componentName).isEqualTo(expectedComponentName);
    }
}
