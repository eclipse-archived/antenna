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

import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ProcessingMessageTest {
    private ProcessingMessage processingMessage;
    private ArtifactIdentifier id;

    @Before
    public void init() {
        this.processingMessage = new ProcessingMessage(MessageType.UNKNOWN_LICENSE);
        id = new ArtifactIdentifier();
        this.processingMessage.setIdentifier(id);
        this.processingMessage.setMessage("test");
    }

    @Test
    public void setterAndGetterTestforLicenseMessage() {
        ProcessingMessage licenseMessage = new ProcessingMessage(MessageType.HANDLE_AS_VALID);
        licenseMessage.setLicenseName("license");
        assertThat(licenseMessage.getLicenseName()).isEqualTo("license");
    }

    @Test
    public void testEquals() {
        ProcessingMessage licenseMessage = new ProcessingMessage(MessageType.HANDLE_AS_VALID);
        licenseMessage.setLicenseName("license");
        assertThat(licenseMessage.equals(licenseMessage)).isTrue();
        assertThat(licenseMessage.equals("")).isFalse();
        ProcessingMessage compareMessage = new ProcessingMessage(MessageType.PROCESSING_FAILURE);
        assertThat(licenseMessage.equals(compareMessage)).isFalse();
        licenseMessage.setIdentifier(new ArtifactIdentifier());
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

    @Test
    public void identifierTest() {
        assertThat(processingMessage.getIdentifier().isPresent());
        assertThat(processingMessage.getIdentifier().get()).isEqualTo(id);
    }

}
