/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.processors;

import com.github.packageurl.MalformedPackageURLException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SW360EnricherTest extends AntennaTestWithMockedContext {
    private List<Artifact> artifacts;
    private SW360Enricher sw360Enricher;
    private SW360MetaDataReceiver connector;

    private String sourceUrl = "https://thrift.apache.org/";
    private String releaseTagUrl = "https://github.com/apache/thrift/releases/tag/0.10.0";
    private String swhID = "swh:1:rel:ae93ff0b4bdbd6749f75c23ad23311b512230894";
    private String hashString = "b2a4d4ae21c789b689dd162deb819665567f481c";
    private String copyrights = "Copyright 2006-2010 The Apache Software Foundation.";

    @Before
    public void setUp() throws IOException {
        when(toolConfigMock.getAntennaTargetDirectory())
                .thenReturn(temporaryFolder.newFolder("target").toPath());
        Artifact artifact0 = new Artifact();
        artifact0.addFact(new ArtifactFilename("filename0"));

        artifacts = Collections.singletonList(artifact0);

        sw360Enricher = new SW360Enricher();
        sw360Enricher.setAntennaContext(antennaContextMock);

        Map<String, String> configMap = new HashMap<>();
        configMap.put("rest.server.url", "rest_url");
        configMap.put("auth.server.url", "auth_url");
        configMap.put("user.id", "username");
        configMap.put("user.password", "password");
        configMap.put("client.id", "client_user");
        configMap.put("client.password", "client_password");
        configMap.put("proxy.use", "false");
        sw360Enricher.configure(configMap);

        connector = Mockito.mock(SW360MetaDataReceiver.class);
        ReflectionTestUtils.setField(sw360Enricher, "connector", connector);
    }

    @After
    public void after() {
        temporaryFolder.delete();

        verify(toolConfigMock, atLeast(0)).getProxyHost();
        verify(toolConfigMock, atLeast(0)).getProxyPort();
    }


    @Test
    public void releaseIsMappedToArtifactCorrectly() throws MalformedPackageURLException {
        SW360Release release0 = mkSW360Release("test1");
        release0.set_Embedded(new SW360ReleaseEmbedded());
        release0.get_Embedded().setLicenses(Collections.emptyList());

        when(connector.findReleaseForArtifact(artifacts.get(0))).thenReturn(Optional.of(release0));

        sw360Enricher.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(1);
        Artifact artifact0 = artifacts.get(0);

        assertThat(artifact0.getCoordinateForType(Coordinate.Types.MAVEN).get().canonicalize()).isEqualTo("pkg:maven/test/test1@1.2.3");

        assertThat(artifact0.askFor(ArtifactSourceUrl.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactSourceUrl.class).get()).isEqualTo(sourceUrl);
        assertThat(artifact0.askFor(ArtifactReleaseTagURL.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactReleaseTagURL.class).get()).isEqualTo(releaseTagUrl);
        assertThat(artifact0.askFor(ArtifactSoftwareHeritageID.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactSoftwareHeritageID.class).get()).isEqualTo(swhID);

        assertThat(artifact0.askFor(DeclaredLicenseInformation.class).isPresent()).isTrue();
        License tempDLicense = new License();
        tempDLicense.setName("Apache-2.0");
        assertThat(artifact0.askForGet(DeclaredLicenseInformation.class).get().getLicenses()).contains(tempDLicense);

        assertThat(artifact0.askFor(ObservedLicenseInformation.class).isPresent()).isTrue();
        License tempOLicense = new License();
        tempOLicense.setName("A-Test-License");
        assertThat(artifact0.askForGet(ObservedLicenseInformation.class).get().getLicenses()).contains(tempOLicense);

        LicenseStatement licenseStatement = new LicenseStatement();
        licenseStatement.setLeftStatement(tempDLicense);
        licenseStatement.setRightStatement(tempOLicense);
        licenseStatement.setOp(LicenseOperator.AND);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact0)).isEqualTo(licenseStatement);

        assertThat(artifact0.askFor(ArtifactFilename.class).isPresent()).isTrue();
        assertThat(artifact0.askFor(ArtifactFilename.class).get().getArtifactFilenameEntries()
                .stream()
                .filter(entry -> entry.getHash() == hashString)
                .collect(Collectors.toList()))
                .hasSize(1);

        assertThat(artifact0.askFor(ArtifactClearingState.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactClearingState.class).get()).isEqualTo(ArtifactClearingState.ClearingState.PROJECT_APPROVED);

        assertThat(artifact0.askFor(ArtifactChangeStatus.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(ArtifactChangeStatus.class).get()).isEqualTo(ArtifactChangeStatus.ChangeStatus.AS_IS);

        assertThat(artifact0.askFor(CopyrightStatement.class).isPresent()).isTrue();
        assertThat(artifact0.askForGet(CopyrightStatement.class).get()).isEqualTo(copyrights);

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

        sw360Enricher.process(artifacts);

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses()).hasSize(1);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().get(0).getName()).isEqualTo("apache2");
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)).getLicenses().get(0).getText()).isEqualTo("Some text");
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
    public void differingLicenseIsOverwritten() {
        License apache = new License();
        apache.setName("apache2");

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

    private SW360Release mkSW360Release(String name) throws MalformedPackageURLException {
        SW360Release sw360Release = new SW360Release();

        sw360Release.setName(name);

        sw360Release.setDownloadurl(sourceUrl);
        sw360Release.setClearingState("PROJECT_APPROVED");


        sw360Release.setDeclaredLicense("Apache-2.0");
        sw360Release.setObservedLicense("A-Test-License");
        sw360Release.setCoordinates(Collections.singletonMap(Coordinate.Types.MAVEN, "pkg:maven/test/test1@1.2.3"));
        sw360Release.setReleaseTagUrl(releaseTagUrl);
        sw360Release.setSoftwareHeritageId(swhID);
        sw360Release.setHashes(Collections.singleton(hashString));
        sw360Release.setChangeStatus("AS_IS");
        sw360Release.setCopyrights(copyrights);

        return sw360Release;
    }
}
