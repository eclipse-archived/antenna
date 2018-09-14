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

public class MessageTest {
    private ProcessingMessage failure;
    private ArtifactIdentifier id;

    @Before
    public void init() {
        this.failure = new ProcessingMessage(MessageType.UNKNOWN_LICENSE);
        id = new ArtifactIdentifier();
        this.failure.setIdentifier(id);
        this.failure.setMessage("test");
    }

    @Test
    public void typeTest() {
        assertThat(failure.getMessageType()).isEqualTo(MessageType.UNKNOWN_LICENSE);
    }

    @Test
    public void messageTest() {
        assertThat(failure.getMessage()).isEqualTo("test");
    }

    @Test
    public void identifierTest() {
        assertThat(failure.getIdentifier().get()).isEqualTo(id);
    }

    @Test
    public void testHash() {
        ProcessingMessage message = new ProcessingMessage(MessageType.DISALLOWED_LICENSE);
        message.hashCode();
    }

}
