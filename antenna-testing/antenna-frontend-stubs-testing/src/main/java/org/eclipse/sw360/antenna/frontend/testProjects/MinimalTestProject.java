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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.testProjects.TestProjectUtils.mkWorkflowStep;

public class MinimalTestProject extends AbstractTestProjectWithExpectations implements NonExecutableTestProject{

    public MinimalTestProject() {
        super();
    }

    @Override
    public String getExpectedProjectArtifactId() {
        return "minimal-test-project";
    }

    @Override
    public String getExpectedProjectVersion() {
        return "1.2-SNAPSHOT";
    }

    @Override
    public String getExpectedToolConfigurationProductName() {
        return "Product Name";
    }

    @Override
    public String getExpectedToolConfigurationProductFullName() {
        return "Super long Product Full Name";
    }

    @Override
    public String getExpectedToolConfigurationProductVersion() {
        return "3.4.0";
    }

    @Override
    public List<String> getExpectedFilesToAttach() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<String> getExpectedToolConfigurationConfigFiles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationAnalyzers() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationGenerators() {
        return new BasicConfiguration().getGenerators(projectRoot.toString());
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationProcessors() {
        return new BasicConfiguration().getProcessors();
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationOutputHandlers() {
        return Collections.emptyList();
    }

    @Override
    public int getExpectedProxyPort() {
        return 0;
    }

    @Override
    public String getExpectedProxyHost() {
        return null;
    }

    @Override
    public boolean getExpectedToolConfigurationMavenInstalled() {
        return false;
    }

    @Override
    public boolean getExpectedToolConfigurationAttachAll() {
        return true;
    }

    @Override
    public boolean getExpectedToolConfigurationSkip() { return false; }

    @Override
    public List<String> getExpectedToolConfigurationConfigFilesEndings() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean requiresMaven() {
        return false;
    }
}
