/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactChangeStatus;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactReleaseTagURL;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSoftwareHeritageID;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseOperator;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360EnricherTest extends AntennaTestWithMockedContext {
    private List<Artifact> artifacts;
    private SW360Enricher sw360Enricher;
    private SW360MetaDataReceiver connector;

    @Before
    public void setUp() throws IOException {
        when(toolConfigMock.getAntennaTargetDirectory())
                .thenReturn(temporaryFolder.newFolder("target").toPath());
        Artifact artifact0 = new Artifact();
        artifact0.addFact(new ArtifactFilename("filename0"));

        artifacts = Collections.singletonList(artifact0);
        connector = Mockito.mock(SW360MetaDataReceiver.class);

        sw360Enricher = new SW360Enricher() {
            @Override
            SW360MetaDataReceiver createMetaDataReceiver(Map<String, String> configMap) {
                return connector;
            }
        };
        sw360Enricher.setAntennaContext(antennaContextMock);
    }

    private static Map<String, String> createStandardConfigMap() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("rest.server.url", "rest_url");
        configMap.put("auth.server.url", "auth_url");
        configMap.put("user.id", "username");
        configMap.put("user.password", "password");
        configMap.put("client.id", "client_user");
        configMap.put("client.password", "client_password");
        configMap.put("proxy.use", "false");
        return configMap;
    }

    @Test
    public void testDefaultConnectionFactoryIsCreated() {
        assertThat(sw360Enricher.getConnectionFactory()).isNotNull();
    }

    @Test
    public void testMetaDataReceiverIsCreated() {
        SW360ConnectionConfigurationFactory connectionFactory = mock(SW360ConnectionConfigurationFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        SW360Connection connection = mock(SW360Connection.class);
        when(antennaContextMock.getHttpClient()).thenReturn(httpClient);
        when(antennaContextMock.getObjectMapper()).thenReturn(mapper);
        when(connectionFactory.createConnection(any(), eq(httpClient), eq(mapper))).thenReturn(connection);

        sw360Enricher = new SW360Enricher(connectionFactory);
        sw360Enricher.setAntennaContext(antennaContextMock);

        SW360MetaDataReceiver metaDataReceiver = sw360Enricher.createMetaDataReceiver(createStandardConfigMap());
        assertThat(metaDataReceiver).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<SW360ConnectionConfigurationFactory.Getter<String>> captor =
                ArgumentCaptor.forClass(SW360ConnectionConfigurationFactory.Getter.class);
        verify(connectionFactory).createConnection(captor.capture(), eq(httpClient), eq(mapper));
        SW360ConnectionConfigurationFactory.Getter<String> getter = captor.getValue();
        for (Map.Entry<String, String> e : createStandardConfigMap().entrySet()) {
            assertThat(getter.apply(e.getKey())).isEqualTo(e.getValue());
        }
        verify(connection).getReleaseAdapter();
        verify(antennaContextMock).getHttpClient();
        verify(antennaContextMock).getObjectMapper();
    }

    @Test
    public void testWithDownloadActivated() throws IOException {
        final Path downloadPath = temporaryFolder.newFolder("download.directory").toPath();
        Map<String, String> configMap = createStandardConfigMap();
        configMap.put("download.attachments", "true");
        configMap.put("download.directory", downloadPath.toString());
        sw360Enricher.configure(configMap);

        final String downloadFilename = "downloadedSource.jar";
        SW360SparseAttachment sparseAttachment = new SW360SparseAttachment()
                .setAttachmentType(SW360AttachmentType.SOURCE)
                .setFilename(downloadFilename);
        SW360ReleaseEmbedded releaseEmbedded = new SW360ReleaseEmbedded();
        releaseEmbedded.setAttachments(Collections.singleton(sparseAttachment));
        SW360Release release0 = new SW360Release();
        release0.set_Embedded(releaseEmbedded);

        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.downloadAttachment(release0, sparseAttachment, downloadPath))
                .thenReturn(Optional.of(Paths.get(downloadFilename)));

        final Collection<Artifact> process = sw360Enricher.process(artifacts);

        final Optional<Artifact> artifact = process.stream().findFirst();
        assertThat(artifact).isPresent();
        assertThat(artifact.get().askFor(ArtifactSourceFile.class)).isPresent();
        assertThat(artifact.get().askForGet(ArtifactSourceFile.class)).hasValue(Paths.get(downloadFilename));
        verify(connector, never()).getLicenseDetails(any());
        verify(toolConfigMock).getAntennaTargetDirectory();
    }

    @Test
    public void releaseIsMappedToArtifactCorrectly() {
        SW360Release release0 = TestUtils.mkSW360Release("test1");
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.emptyList());

        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));

        sw360Enricher.configure(createStandardConfigMap());
        sw360Enricher.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(1);
        Artifact artifact0 = artifacts.get(0);

        assertThat(artifact0.getCoordinateForType(Coordinate.Types.MAVEN).get().canonicalize()).isEqualTo("pkg:maven/org.group.id/artifactIdtest1@" + TestUtils.RELEASE_VERSION1);

        assertThat(artifact0.askFor(ArtifactSourceUrl.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactSourceUrl.class).get()).isEqualTo(TestUtils.RELEASE_DOWNLOAD_URL);
        assertThat(artifact0.askFor(ArtifactReleaseTagURL.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactReleaseTagURL.class).get()).isEqualTo(TestUtils.RELEASE_RELEASE_TAG_URL);
        assertThat(artifact0.askFor(ArtifactSoftwareHeritageID.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactSoftwareHeritageID.class).get()).isEqualTo(TestUtils.RELEASE_SOFTWAREHERITGAE_ID);

        assertThat(artifact0.askFor(DeclaredLicenseInformation.class).isPresent()).isTrue();
        License tempDLicense = new License();
        tempDLicense.setId(TestUtils.RELEASE_DECLEARED_LICENSE);
        assertThat(artifact0.askForGet(DeclaredLicenseInformation.class).get().getLicenses()).contains(tempDLicense);

        assertThat(artifact0.askFor(ObservedLicenseInformation.class).isPresent()).isTrue();
        License tempOLicense = new License();
        tempOLicense.setId(TestUtils.RELEASE_OBSERVED_LICENSE);
        assertThat(artifact0.askForGet(ObservedLicenseInformation.class).get().getLicenses()).contains(tempOLicense);

        LicenseStatement licenseStatement = new LicenseStatement(Stream.of(tempDLicense, tempOLicense)
                .collect(Collectors.toList()), LicenseOperator.AND);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact0)).isEqualTo(licenseStatement);

        assertThat(artifact0.askFor(ArtifactFilename.class).isPresent()).isTrue();
        assertThat(artifact0.askFor(ArtifactFilename.class).get().getArtifactFilenameEntries()
                .stream()
                .filter(entry -> entry.getHash() == TestUtils.RELEASE_HASH1)
                .collect(Collectors.toList()))
                .hasSize(1);

        assertThat(artifact0.askFor(ArtifactClearingState.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactClearingState.class).get()).isEqualTo(ArtifactClearingState.ClearingState.PROJECT_APPROVED);

        assertThat(artifact0.askFor(ArtifactChangeStatus.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactChangeStatus.class).get()).isEqualTo(ArtifactChangeStatus.ChangeStatus.AS_IS);

        assertThat(artifact0.askFor(CopyrightStatement.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(CopyrightStatement.class).get()).isEqualTo(TestUtils.RELEASE_COPYRIGHT);

    }

    @Test
    public void testWhenNoReleaseIsFoundInFindRelease() {
        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());

        sw360Enricher.configure(createStandardConfigMap());
        final Collection<Artifact> process = sw360Enricher.process(artifacts);

        assertThat(process).containsExactlyElementsOf(artifacts);
        verify(reporterMock, times(1)).add(eq(artifacts.get(0)), any(), any());
    }

    @Test
    public void singleLicenseIsAddedToArtifact() {
        SW360SparseLicense apacheSparse = createSparseLicense("apache2", "Apache 2.0");
        SW360License apache = createLicenseFromSparseLicense(apacheSparse, "Some text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.singletonList(apacheSparse));
        release0.setObservedLicense("apache2");

        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(apacheSparse)).thenReturn(Optional.of(apache));

        sw360Enricher.configure(createStandardConfigMap());
        sw360Enricher.process(artifacts);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses()).hasSize(1);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().iterator().next().getId()).isEqualTo("apache2");
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().iterator().next().getText()).isEqualTo("Some text");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void multipleLicensesAreAddedToArtifact() {
        SW360SparseLicense apacheSparse = createSparseLicense("apache2", "Apache 2.0");
        SW360License apache = createLicenseFromSparseLicense(apacheSparse, "Some text");

        SW360SparseLicense mitSparse = createSparseLicense("mit", "MIT License");
        SW360License mit = createLicenseFromSparseLicense(mitSparse, "MIT license text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Arrays.asList(apacheSparse, mitSparse));
        release0.setDeclaredLicense("apache2 AND mit");

        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(any())).thenReturn(Optional.of(apache), Optional.of(mit));

        sw360Enricher.configure(createStandardConfigMap());
        sw360Enricher.process(artifacts);

        List<String> licenseNames = ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0))
                .getLicenses()
                .stream()
                .map(LicenseInformation::evaluate)
                .collect(Collectors.toList());
        assertThat(licenseNames).hasSize(2);
        assertThat(licenseNames).contains("apache2", "mit");
    }

    @Test
    public void differingLicenseIsOverwritten() {
        License apache = new License();
        apache.setId("apache2");

        artifacts.get(0).addFact(new DeclaredLicenseInformation(apache));

        SW360SparseLicense mitSparse = createSparseLicense("mit", "MIT License");
        SW360License mit = createLicenseFromSparseLicense(mitSparse, "MIT license text");

        SW360Release release0 = new SW360Release();
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.singletonList(mitSparse));
        release0.setDeclaredLicense("mit");

        when(connector.findReleaseForArtifact(any())).thenReturn(Optional.empty());
        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));
        when(connector.getLicenseDetails(any())).thenReturn(Optional.of(mit));

        sw360Enricher.configure(createStandardConfigMap());
        sw360Enricher.process(artifacts);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses()).hasSize(1);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().iterator().next().evaluate()).isEqualTo("mit");
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
