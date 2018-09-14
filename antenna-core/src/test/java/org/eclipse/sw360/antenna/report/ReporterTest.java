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
package org.eclipse.sw360.antenna.report;

import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;

import static org.fest.assertions.Assertions.assertThat;

public class ReporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Reporter reporter;
    private ArtifactIdentifier id;
    private String msg;

    @Before
    public void init(){
        reporter = new Reporter(folder.getRoot().toPath());

        id = new ArtifactIdentifier();
        id.setFilename("testfile");
        id.setHash("testhash");
        MavenCoordinates mvnCrds = new MavenCoordinates();
        mvnCrds.setArtifactId("testAid");
        mvnCrds.setGroupId("testGid");
        mvnCrds.setVersion("testVer");
        id.setMavenCoordinates(mvnCrds);

        msg = "Some processing message message";
    }

    @Test
    public void testAddMessageWithArtifactIdentifier() {
        reporter.addProcessingMessage(id, MessageType.MISSING_SOURCES, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getIdentifier().isPresent());
        assertThat(processingMessage.getIdentifier().get())
                .isEqualTo(id);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.MISSING_SOURCES);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString.contains(msg));
        assertThat(reportString.contains(id.getMavenCoordinates().getVersion()));
    }

    @Test
    public void testAddMessageWithNullArtifactIdentifier() {
        reporter.addProcessingMessage(new ArtifactIdentifier(), MessageType.MISSING_COORDINATES, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.MISSING_COORDINATES);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString.contains(msg));
    }

    @Test
    public void testAddMessageWithoutArtifactIdentifier() {
        reporter.addProcessingMessage(MessageType.UNKNOWN_LICENSE, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.UNKNOWN_LICENSE);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString.contains(msg));
    }

    @Test
    public void testAddMessageWithLicense() {
        String license = "EPL-1.0";
        reporter.add(license, MessageType.UNKNOWN_LICENSE, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getLicenseName())
                .isEqualTo(license);
        assertThat(processingMessage.getMessageType())
                .isEqualTo(MessageType.UNKNOWN_LICENSE);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString.contains(msg));
        assertThat(reportString.contains(id.getMavenCoordinates().getVersion()));
        assertThat(reportString.contains(license));
    }

}
