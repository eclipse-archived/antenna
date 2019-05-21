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
package org.eclipse.sw360.antenna.frontend.testProjects;

import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.List;

public abstract class AbstractTestProjectWithExpectations extends AbstractTestProject {
    public AbstractTestProjectWithExpectations() {
        super();
    }

    // Expected parsed parameters:
    abstract public String getExpectedProjectVersion();
    abstract public String getExpectedToolConfigurationProductName();
    abstract public String getExpectedToolConfigurationProductFullName();
    abstract public String getExpectedToolConfigurationProductVersion();
    abstract public List<String> getExpectedFilesToAttach();
    abstract public List<String> getExpectedToolConfigurationConfigFiles();
    abstract public List<WorkflowStep> getExpectedToolConfigurationAnalyzers();
    abstract public List<WorkflowStep> getExpectedToolConfigurationGenerators();
    abstract public List<WorkflowStep> getExpectedToolConfigurationProcessors();
    public abstract List<WorkflowStep> getExpectedToolConfigurationOutputHandlers();
    abstract public int getExpectedProxyPort();
    abstract public String getExpectedProxyHost();
    abstract public boolean getExpectedToolConfigurationMavenInstalled();
    abstract public boolean getExpectedToolConfigurationAttachAll();
    abstract public boolean getExpectedToolConfigurationSkip();
    abstract public List<String> getExpectedToolConfigurationConfigFilesEndings();
    abstract public boolean requiresMaven();
    public boolean getExpectedConfigurationFailOnIncompleteSources() {
        return false;
    }
    public boolean getExpectedConfigurationFailOnMissingSources() {
        return false;
    }
}
