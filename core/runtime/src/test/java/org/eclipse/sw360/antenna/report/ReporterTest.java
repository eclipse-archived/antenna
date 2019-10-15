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

import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Reporter reporter;
    private ArtifactCoordinates id;
    private String msg;

    @Before
    public void init(){
        reporter = new Reporter(folder.getRoot().toPath());

        id = new ArtifactCoordinates(new Coordinate(Coordinate.Types.MAVEN, "testGid", "testAid", "testVer"));

        msg = "Some processing message message";
    }

    @Test
    public void testAddMessageWithArtifactIdentifier() {
        reporter.add(id, MessageType.MISSING_SOURCES, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getIdentifier())
                .isEqualTo(id.toString());
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.MISSING_SOURCES);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString).contains(msg);
        assertThat(reportString).contains(id.getMainCoordinate().getVersion());
    }

    @Test
    public void testAddMessageWithNullArtifactIdentifier() {
        reporter.add(new ArtifactCoordinates(new Coordinate("Name","Version")), MessageType.MISSING_COORDINATES, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.MISSING_COORDINATES);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString).contains(msg);
    }

    @Test
    public void testAddMessageWithoutArtifactIdentifier() {
        reporter.add(MessageType.UNKNOWN_LICENSE, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.UNKNOWN_LICENSE);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString).contains(msg);
    }

    @Test
    public void testAddMessageWithLicense() {
        String license = "EPL-1.0";
        reporter.add(license, MessageType.UNKNOWN_LICENSE, msg);

        final ProcessingMessage processingMessage = reporter.getProcessingReport().getMessageList().get(0);
        assertThat(processingMessage.getIdentifier()).isEqualTo(license);
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.UNKNOWN_LICENSE);
        assertThat(processingMessage.getMessage()).isEqualTo(msg);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reporter.writeReport(stream);
        String reportString = new String(stream.toByteArray());
        assertThat(reportString).contains(msg);
        assertThat(reportString).contains(license);
    }

}
