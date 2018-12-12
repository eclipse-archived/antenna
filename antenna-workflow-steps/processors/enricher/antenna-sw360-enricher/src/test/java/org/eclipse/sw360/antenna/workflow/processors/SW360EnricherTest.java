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

package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360EnricherTest extends AntennaTestWithMockedContext {
    private List<Artifact> artifacts;
    private SW360Enricher sw360Enricher;
    private SW360MetaDataReceiver connector;

    @Before
    public void setUp() throws AntennaConfigurationException {
        Artifact artifact0 = new Artifact();
        artifact0.addFact(new ArtifactFilename("filename0"));

        artifacts = Collections.singletonList(artifact0);

        sw360Enricher = new SW360Enricher();
        sw360Enricher.setAntennaContext(antennaContextMock);

        Map<String, String> configMap = new HashMap<>();
        configMap.put("rest.server.url", "rest_url");
        configMap.put("auth.server.url", "auth_url");
        configMap.put("username", "username");
        configMap.put("password", "password");
        sw360Enricher.configure(configMap);

        connector = Mockito.mock(SW360MetaDataReceiver.class);
        ReflectionTestUtils.setField(sw360Enricher, "connector", connector);
    }

    @Test
    public void singleLicenseIsAddedToArtifact() throws AntennaException, IOException {
        SW360SparseLicense apacheSparse = createSparseLicense("apache2", "Apache 2.0");
        SW360License apache = createLicenseFromSparseLicense(apacheSparse, "Some text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.singletonList(apacheSparse));
        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(apacheSparse)).thenReturn(Optional.of(apache));

        sw360Enricher.process(artifacts);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses()).hasSize(1);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().get(0).getName()).isEqualTo("apache2");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void multipleLicensesAreAddedToArtifact() throws AntennaException, IOException {
        SW360SparseLicense apacheSparse = createSparseLicense("apache2", "Apache 2.0");
        SW360License apache = createLicenseFromSparseLicense(apacheSparse, "Some text");

        SW360SparseLicense mitSparse = createSparseLicense("mit", "MIT License");
        SW360License mit = createLicenseFromSparseLicense(mitSparse, "MIT license text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Arrays.asList(apacheSparse, mitSparse));
        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(any())).thenReturn(Optional.of(apache), Optional.of(mit));

        sw360Enricher.process(artifacts);

        List<String> licenseNames = ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0))
                .getLicenses()
                .stream()
                .map(License::getName)
                .collect(Collectors.toList());
        assertThat(licenseNames).hasSize(2);
        assertThat(licenseNames).contains("apache2", "mit");
    }

    @Test
    public void differingLicenseIsOverwritten() throws AntennaException, IOException {
        License apache = new License();
        apache.setName("apache2");
        apache.setLongName("Apache 2.0");
        apache.setText("Apache license text");
        artifacts.get(0).addFact(new DeclaredLicenseInformation(apache));

        SW360SparseLicense mitSparse = createSparseLicense("mit", "MIT License");
        SW360License mit = createLicenseFromSparseLicense(mitSparse, "MIT license text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.singletonList(mitSparse));
        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(any())).thenReturn(Optional.of(mit));

        sw360Enricher.process(artifacts);

        verify(reporterMock).add(any(Artifact.class), any(), any());
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses()).hasSize(1);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().get(0).getName()).isEqualTo("mit");
    }

    private SW360SparseLicense createSparseLicense(String name, String fullName) {
        SW360SparseLicense sparseLicense = new SW360SparseLicense();
        sparseLicense.setFullName(fullName);
        sparseLicense.set_Links(new LinkObjects());
        sparseLicense.get_Links().setSelf(new Self());
        sparseLicense.get_Links().getSelf().setHref("rest_url/" + name);
        return sparseLicense;
    }

    private SW360License createLicenseFromSparseLicense(SW360SparseLicense sparseLicense, String text) {
        SW360License license = new SW360License();
        license.setFullName(sparseLicense.getFullName());
        license.setShortName(sparseLicense.getShortName());
        license.setText(text);
        license.set_Links(sparseLicense.get_Links());
        return license;
    }
}
