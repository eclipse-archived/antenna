/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.bundle;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.util.HashSet;
import java.util.Set;

public class DroolsEvaluationResult implements IEvaluationResult {
    private String id;
    private Severity severity;
    private String description;
    private Set<Artifact> failedArtifacts = new HashSet<>();

    public DroolsEvaluationResult(String id, String description, Severity severity) {
        this(id, description, severity, null);
    }

    public DroolsEvaluationResult(String id, String description, Severity severity, Set<Artifact> artifacts) {
        this.id = id;
        this.description = description;
        this.severity = severity;
        if (artifacts != null) {
            this.failedArtifacts = artifacts;
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    @Override
    public Set<Artifact> getFailedArtifacts() {
        return this.failedArtifacts;
    }

    public void addFailedArtifact(Artifact a) {
        this.failedArtifacts.add(a);
    }
}
