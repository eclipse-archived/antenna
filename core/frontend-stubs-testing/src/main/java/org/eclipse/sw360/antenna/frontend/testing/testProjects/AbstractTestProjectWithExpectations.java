/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.testing.testProjects;

import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.List;

public abstract class AbstractTestProjectWithExpectations extends AbstractTestProject {
    public AbstractTestProjectWithExpectations() {
        super();
    }

    // Expected parsed parameters:
    public abstract String getExpectedProjectVersion();
    public abstract String getExpectedToolConfigurationProductName();
    public abstract String getExpectedToolConfigurationProductFullName();
    public abstract String getExpectedToolConfigurationProductVersion();
    public abstract List<String> getExpectedFilesToAttach();
    public abstract List<String> getExpectedToolConfigurationConfigFiles();
    public abstract List<WorkflowStep> getExpectedToolConfigurationAnalyzers();
    public abstract List<WorkflowStep> getExpectedToolConfigurationGenerators();
    public abstract List<WorkflowStep> getExpectedToolConfigurationProcessors();
    public abstract List<WorkflowStep> getExpectedToolConfigurationOutputHandlers();
    public abstract int getExpectedProxyPort();
    public abstract String getExpectedProxyHost();
    public abstract boolean getExpectedToolConfigurationMavenInstalled();
    public abstract boolean getExpectedToolConfigurationAttachAll();
    public abstract boolean getExpectedToolConfigurationSkip();
    public abstract List<String> getExpectedToolConfigurationConfigFilesEndings();
    public abstract boolean requiresMaven();
    public boolean getExpectedConfigurationFailOnIncompleteSources() {
        return false;
    }
    public boolean getExpectedConfigurationFailOnMissingSources() {
        return false;
    }
}
