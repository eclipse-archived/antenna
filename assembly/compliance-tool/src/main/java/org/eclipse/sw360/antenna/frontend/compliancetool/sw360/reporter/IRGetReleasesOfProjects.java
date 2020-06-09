/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.*;

public class IRGetReleasesOfProjects implements InfoRequest<SW360SparseRelease> {
    private static final String GET_RELEASES_OF_PROJECT = "releases-of-project";
    private static final String PROJECT_NAME = "--project_name";
    private static final String PROJECT_VERSION = "--project_version";
    private static final String PROJECT_ID = "--project_id";
    private String projectName;
    private String projectVersion;
    private String projectId;

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_OF_PROJECT;
    }

    @Override
    public String helpMessage() {
        return null;
    }

    @Override
    public boolean isValid() {
        if (projectId != null && !projectId.isEmpty()) {
            return true;
        } else return projectName != null && !projectName.isEmpty() &&
                projectVersion != null && !projectVersion.isEmpty();
    }

    @Override
    public Set<String> getAdditionalParameters() {
        Set<String> additionalParameters = new HashSet<>();
        additionalParameters.add(PROJECT_ID);
        additionalParameters.add(PROJECT_NAME);
        additionalParameters.add(PROJECT_VERSION);
        return additionalParameters;
    }

    @Override
    public void parseAdditionalParameter(Map<String, String> parameters) {
        projectId = ReporterParameterParser.parseParameterValueFromMapOfParameters(parameters, getProjectIdParameter());

        projectName = ReporterParameterParser.parseParameterValueFromMapOfParameters(parameters, getProjectNameParameter());

        projectVersion = ReporterParameterParser.parseParameterValueFromMapOfParameters(parameters, getProjectVersionParameter());
    }

    @Override
    public Collection<SW360SparseRelease> execute(SW360Connection connection) {
        Collection<SW360SparseRelease> result;
        if (projectId != null && !projectId.isEmpty()) {
            result = connection.getProjectAdapter().getLinkedReleases(projectId, true);
            return result;
        } else if (projectName != null && !projectName.isEmpty() &&
                projectVersion != null && !projectVersion.isEmpty()) {
            final Optional<SW360Project> projectIdByNameAndVersion = connection.getProjectAdapter().getProjectByNameAndVersion(projectName, projectVersion);
            if (projectIdByNameAndVersion.isPresent()) {
                result = connection.getProjectAdapter().getLinkedReleases(projectIdByNameAndVersion.get().getId(), true);
                return result;
            } else {
                throw new IllegalArgumentException("Project " + projectName + " with version " + projectVersion + " could not be found.");
            }
        } else {
            throw new IllegalArgumentException("The provided parameters did not provide enough information to execute your request.");
        }
    }

    @Override
    public Class<SW360SparseRelease> getType() {
        return SW360SparseRelease.class;
    }

    private String getProjectIdParameter() {
        return PROJECT_ID;
    }

    private String getProjectNameParameter() {
        return PROJECT_NAME;
    }

    private String getProjectVersionParameter() {
        return PROJECT_VERSION;
    }
}