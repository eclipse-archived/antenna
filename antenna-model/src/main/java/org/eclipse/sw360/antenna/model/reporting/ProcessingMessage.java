/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model.reporting;

/**
 * Describes a message which gives information about an event that occurred
 * during processing. The ProcessingMessage has a MessageType, a message and
 * either an ArtifactIdentifier or a license name.
 */
public class ProcessingMessage {
    private final MessageType messageType;
    private final String identifier;
    private final String message;

    public ProcessingMessage(MessageType type, String message) {
        this.messageType = type;
        this.identifier = null;
        this.message = message;
    }

    public ProcessingMessage(MessageType type, String identifier, String message) {
        this.messageType = type;
        this.identifier = identifier;
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    // CSOFF
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProcessingMessage other = (ProcessingMessage) obj;
        if (messageType == null) {
            if (other.messageType != null) {
                return false;
            }
        } else if (!messageType.equals(other.messageType)) {
            return false;
        }
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        return true;
    }
    // CSON
}
