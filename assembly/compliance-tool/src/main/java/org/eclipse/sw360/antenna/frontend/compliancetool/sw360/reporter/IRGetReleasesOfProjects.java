package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class IRGetReleasesOfProjects extends InfoRequest<SW360SparseRelease> {
    private static final String GET_RELEASES_OF_PROJECT = ReporterParameterParser.REPORTER_PARAMETER_PREFIX + "releases-of-project";
    private static final String PROJECT_NAME = "--project_name";
    private static final String PROJECT_VERSION = "--project-version";
    private static final String PROJECT_ID = "--project_id";
    private String projectName;
    private String projectVersion;
    private String projectId;

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_OF_PROJECT;
    }

    @Override
    String helpMessage() {
        return null;
    }

    @Override
    boolean isValid() {
        if (projectId != null && !projectId.isEmpty()) {
            return true;
        } else return projectName != null && !projectName.isEmpty() &&
                projectVersion != null && !projectVersion.isEmpty();
    }

    @Override
    Set<String> getAdditionalParameters() {
        Set<String> additionalParamaters = new HashSet<>();
        additionalParamaters.add(PROJECT_ID);
        additionalParamaters.add(PROJECT_NAME);
        additionalParamaters.add(PROJECT_VERSION);
        return additionalParamaters;
    }

    @Override
    void parseAdditionalParameter(Set<String> parameters) {
        System.out.println(parameters);
        projectId = ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, getProjectIdParameter());

        projectName = ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, getProjectNameParameter());

        projectVersion = ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, getProjectVersionParameter());
    }

    @Override
    Collection<SW360SparseRelease> execute(SW360Connection connection) {
        Collection<SW360SparseRelease> result;
        if (projectId != null && !projectId.isEmpty()) {
            result = connection.getProjectAdapter().getLinkedReleases(projectId);
            return result;
        } else if (projectName != null && !projectName.isEmpty() &&
            projectVersion != null && !projectVersion.isEmpty()) {
            final Optional<String> projectIdByNameAndVersion = connection.getProjectAdapter().getProjectIdByNameAndVersion(projectName, projectVersion);
            if (projectIdByNameAndVersion.isPresent()) {
                result = connection.getProjectAdapter().getLinkedReleases(projectIdByNameAndVersion.get());
                return result;
            } else {
                throw new IllegalArgumentException("Project " + projectName + " with version " + projectVersion + " could not be found.");
            }
        } else {
            throw new IllegalArgumentException("The provided parameters did provide enough information to execute your request.");
        }
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