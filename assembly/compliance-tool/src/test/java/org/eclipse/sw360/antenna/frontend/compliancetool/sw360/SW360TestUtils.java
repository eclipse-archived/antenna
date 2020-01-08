/*
 * Copyright (c) Bosch Software Innovations GmbH 2020.
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
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class SW360TestUtils {
    public static SW360Release mkSW360Release(String name) {
        SW360Release sw360Release = new SW360Release();

        sw360Release.setName(name);
        sw360Release.setVersion("1.0.0");

        sw360Release.setDownloadurl("https://thrift.apache.org/");
        sw360Release.setClearingState("PROJECT_APPROVED");

        sw360Release.setDeclaredLicense("Apache-2.0");
        sw360Release.setObservedLicense("A-Test-License");
        sw360Release.setCoordinates(Collections.singletonMap(Coordinate.Types.MAVEN, "pkg:maven/test/test1@1.2.3"));
        sw360Release.setReleaseTagUrl("https://github.com/apache/thrift/releases/tag/0.10.0");
        sw360Release.setSoftwareHeritageId("swh:1:rel:ae93ff0b4bdbd6749f75c23ad23311b512230894");
        sw360Release.setHashes(Collections.singleton("b2a4d4ae21c789b689dd162deb819665567f481c"));
        sw360Release.setChangeStatus("AS_IS");
        sw360Release.setCopyrights("Copyright 2006-2010 The Apache Software Foundation.");

        return sw360Release;
    }

    public static SW360SparseRelease mkSW3SparseRelease(String name) {
        SW360SparseRelease sparseRelease = new SW360SparseRelease();
        sparseRelease.setName(name);
        sparseRelease.setVersion("1.0.0");
        Self self = new Self("http://localhost:8080/releases/12345");
        LinkObjects linkObjectsWithSelf = new LinkObjects()
                .setSelf(self);
        sparseRelease.set_Links(linkObjectsWithSelf);
        return sparseRelease;
    }

    public static SW360Component mkSW360Component(String name) {
        SW360Component component = new SW360Component();
        component.setName(name);
        component.setComponentType(SW360ComponentType.INTERNAL);
        SW360ComponentEmbedded componentEmbedded = new SW360ComponentEmbedded();
        componentEmbedded.setReleases(Collections.singletonList(mkSW3SparseRelease(name)));
        component.set_Embedded(componentEmbedded);
        return component;
    }

    public static SW360SparseComponent mkSW360SparseComponent(String name) {
        SW360SparseComponent sparseComponent = new SW360SparseComponent();
        sparseComponent.setName(name);
        sparseComponent.setComponentType(SW360ComponentType.INTERNAL);
        Self self = new Self("http://localhost:8080/components/12345");
        LinkObjects linkObjectsWithSelf = new LinkObjects()
                .setSelf(self);
        sparseComponent.set_Links(linkObjectsWithSelf);
        return sparseComponent;
    }

    public static CSVParser getCsvParser(File currentCsvFile) throws IOException {
        FileInputStream fs = new FileInputStream(currentCsvFile);
        InputStreamReader isr = new InputStreamReader(fs, StandardCharsets.UTF_8);
        CSVFormat csvFormat = CSVFormat.DEFAULT;
        csvFormat = csvFormat.withFirstRecordAsHeader();
        csvFormat = csvFormat.withDelimiter(',');
        return new CSVParser(isr, csvFormat);
    }
}
