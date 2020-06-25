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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class SW360TestUtils {
    private static final String RELEASE_VERSION1 = "1.0.0";
    private static final String RELEASE_DOWNLOAD_URL = "https://organisation-test.org/";
    private static final String RELEASE_CLEARING_STATE = "PROJECT_APPROVED";
    private static final String RELEASE_DECLARED_LICENSE = "The-Test-License";
    private static final String RELEASE_OBSERVED_LICENSE = "A-Test-License";
    private static final String RELEASE_RELEASE_TAG_URL = "https://gitTool.com/project/repository";
    private static final String RELEASE_MAVEN_COORDINATES = "pkg:maven/test/test1@1.0.0";
    private static final String RELEASE_SOFTWAREHERITGAE_ID = "swh:1:rel:1234512345123451234512345123451234512345";
    private static final String RELEASE_HASH1= "b2a4d4ae21c789b689dd162deb819665567f481c";
    private static final String RELEASE_CHANGESTATUS = "AS_IS";
    private static final String RELEASE_COPYRIGHT = "Copyright xxxx Some Copyright Enterprise";

    private static final SW360ComponentType COMPONENT_TYPE = SW360ComponentType.INTERNAL;

    private static final String RESOURCE_URI_PREFIX = "https://sw360.org/api/resources";

    public static SW360Release mkSW360Release(String name) {
        SW360Release sw360Release = new SW360Release();

        sw360Release.setName(name);
        sw360Release.setVersion(RELEASE_VERSION1);
        sw360Release.setCreatedOn("yyyy-mm-dd");

        sw360Release.setDownloadurl(RELEASE_DOWNLOAD_URL);
        sw360Release.setClearingState(RELEASE_CLEARING_STATE);

        sw360Release.setDeclaredLicense(RELEASE_DECLARED_LICENSE);
        sw360Release.setObservedLicense(RELEASE_OBSERVED_LICENSE);
        sw360Release.setCoordinates(Collections.singletonMap(Coordinate.Types.MAVEN, RELEASE_MAVEN_COORDINATES));
        sw360Release.setReleaseTagUrl(RELEASE_RELEASE_TAG_URL);
        sw360Release.setSoftwareHeritageId(RELEASE_SOFTWAREHERITGAE_ID);
        sw360Release.setHashes(Collections.singleton(RELEASE_HASH1));
        sw360Release.setChangeStatus(RELEASE_CHANGESTATUS);
        sw360Release.setCopyrights(RELEASE_COPYRIGHT);

        sw360Release.setEmbedded(mkReleaseEmbedded(name));

        return sw360Release;
    }

    private static SW360ReleaseEmbedded mkReleaseEmbedded(String name) {
        SW360ReleaseEmbedded sw360ReleaseEmbedded = new SW360ReleaseEmbedded();
        sw360ReleaseEmbedded.setAttachments
                (Collections.singleton(mkAttachment(name)));
        return sw360ReleaseEmbedded;
    }

    public static SW360SparseAttachment mkAttachment(String name) {
        return initResourceId(new SW360SparseAttachment()
                .setAttachmentType(SW360AttachmentType.SOURCE)
                .setFilename(name + "sources.zip"));
    }

    public static SW360SparseRelease mkSW3SparseRelease(String name) {
        SW360SparseRelease sparseRelease = initResourceId(new SW360SparseRelease());
        sparseRelease.setName(name);
        sparseRelease.setVersion(RELEASE_VERSION1);
        return sparseRelease;
    }

    public static <L extends LinkObjects, E extends Embedded, T extends SW360HalResource<L, E>> T
    initSelfLink(T resource, String href) {
        resource.getLinks().setSelf(new Self(href));
        return resource;
    }

    public static <L extends LinkObjects, E extends Embedded, T extends SW360HalResource<L, E>> T
    initResourceId(T resource) {
        String href = RESOURCE_URI_PREFIX + resource.getClass().getSimpleName().toLowerCase(Locale.ROOT) +
                "s/" + System.identityHashCode(resource);
        return initSelfLink(resource, href);
    }

    public static SW360Component mkSW360Component(String name) {
        SW360Component component = new SW360Component();
        component.setName(name);
        component.setComponentType(COMPONENT_TYPE);
        SW360ComponentEmbedded componentEmbedded = new SW360ComponentEmbedded();
        componentEmbedded.setReleases(Arrays.asList(mkSW3SparseRelease(name), mkSW3SparseRelease(name)));
        component.setEmbedded(componentEmbedded);
        return component;
    }

    public static SW360SparseComponent mkSW360SparseComponent(String name) {
        SW360SparseComponent sparseComponent = initResourceId(new SW360SparseComponent());
        sparseComponent.setName(name);
        sparseComponent.setComponentType(COMPONENT_TYPE);
        return sparseComponent;
    }

    public static CSVParser getCsvParser(File currentCsvFile, char delimiter) throws IOException {
        FileInputStream fs = new FileInputStream(currentCsvFile);
        InputStreamReader isr = new InputStreamReader(fs, StandardCharsets.UTF_8);
        CSVFormat csvFormat = CSVFormat.DEFAULT;
        csvFormat = csvFormat.withFirstRecordAsHeader();
        csvFormat = csvFormat.withDelimiter(delimiter);
        return new CSVParser(isr, csvFormat);
    }
}
