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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ProcessingMessageTest {
    private ProcessingMessage processingMessage;
    private ArtifactIdentifier id;

    @Before
    public void init() {
        id = new GenericArtifactCoordinates("Name", "Version");
        this.processingMessage = new ProcessingMessage(MessageType.UNKNOWN_LICENSE, id.toString(), "test");
    }

    @Test
    public void setterAndGetterTestforLicenseMessage() {
        ProcessingMessage licenseMessage = new ProcessingMessage(MessageType.HANDLE_AS_VALID, "license", "msg");
        assertThat(licenseMessage.getIdentifier()).isEqualTo("license");
    }

    @Test
    public void testEquals() {
        ProcessingMessage licenseMessage = new ProcessingMessage(MessageType.HANDLE_AS_VALID, "license", "msg");
        assertThat(licenseMessage).isEqualTo(licenseMessage);
        assertThat(licenseMessage).isNotEqualTo("");
        ProcessingMessage compareMessage = new ProcessingMessage(MessageType.PROCESSING_FAILURE, new GenericArtifactCoordinates("Name", "Version").toString(), "msg");
        assertThat(licenseMessage.equals(compareMessage)).isFalse();
    }

    @Test
    public void typeTest() {
        assertThat(processingMessage.getMessageType()).isEqualTo(MessageType.UNKNOWN_LICENSE);
    }

    @Test
    public void messageTest() {
        assertThat(processingMessage.getMessage()).isEqualTo("test");
    }
}
