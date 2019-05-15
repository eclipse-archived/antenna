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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Special ProcessingMessage for incomplete source jars.
 */
public class IncompleteSourcesFailure extends ProcessingMessageWithPayload {
    private List<String> missingSources = new ArrayList<>();

    public IncompleteSourcesFailure(String message) {
        super(MessageType.INCOMPLETE_SOURCES, message);
    }

    public IncompleteSourcesFailure(String identifier, String message) {
        super(MessageType.INCOMPLETE_SOURCES, identifier, message);
    }

    public void addMissingSources(String className) {
        missingSources.add(className);
    }

    public void addMissingSources(Collection<String> classNames) {
        missingSources.addAll(classNames);
    }

    @Override
    public List<String> getPayload() {
        return missingSources;
    }
}
