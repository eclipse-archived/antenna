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

import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Special ProcessingMessage for incomplete source jars.
 */
public class IncompleteSourcesFailure extends ProcessingMessage {
    /**
     * Super constructor is used.
     */
    public IncompleteSourcesFailure() {
        super(MessageType.INCOMPLETE_SOURCES);
    }

    private List<String> missingSources = new ArrayList<>();

    public void setMissingSources(List<String> missingSources) {
        this.missingSources = missingSources;
    }

    public List<String> getMissingSources() {
        return missingSources;
    }

    /**
     * The name of the missing class is added to the list of missing classes.
     * 
     * @param className
     */
    public void addMissingSources(String className) {
        missingSources.add(className);
    }
}
