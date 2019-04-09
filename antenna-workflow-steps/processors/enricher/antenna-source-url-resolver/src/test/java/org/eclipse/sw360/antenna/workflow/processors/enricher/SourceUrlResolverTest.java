/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.processors.enricher;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.util.HttpHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.setVariableValueInObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SourceUrlResolverTest {
    @Mock
    private HttpHelper httpHelper;

    private SourceUrlResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = new SourceUrlResolver();
        setVariableValueInObject(resolver, "httpHelper", httpHelper);
    }

    @Test
    public void processDownloadsSourcesAndSetsTheSourceFileFact() throws Exception {
        when(httpHelper.downloadFile(eq("https://example.com/artifact0.zip"), any()))
                .thenReturn(new File("artifact0.zip"));

        Artifact artifact0 = new Artifact();
        artifact0.addFact(new ArtifactSourceUrl("https://example.com/artifact0.zip"));

        Artifact artifact1 = new Artifact();

        Collection<Artifact> artifacts = Arrays.asList(artifact0, artifact1);

        resolver.process(artifacts);

        assertThat(artifact0.askForGet(ArtifactSourceFile.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactSourceFile.class).get().toString()).isEqualTo("artifact0.zip");

        assertThat(artifact1.askForGet(ArtifactSourceFile.class).isPresent()).isFalse();
    }
}