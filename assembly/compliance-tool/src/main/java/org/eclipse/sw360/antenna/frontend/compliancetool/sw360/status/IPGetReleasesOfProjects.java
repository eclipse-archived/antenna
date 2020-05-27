package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class IPGetReleasesOfProjects extends InfoParameter {
    static final String GET_RELEASES_OF_PROJECT = SW360StatusReporterParameters.REPORTER_PARAMETER_PREFIX + "releases-of-project";
    static final String PROJECT_NAME = "--project_name";
    static final String PROJECT_VERSION = "--project-version";
    static final String PROJECT_ID = "--project_id";
    private String projectName;
    private String projectVersion;
    private String projectId;

    private Collection<SW360SparseRelease> result;

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
        } else if (projectName != null && !projectName.isEmpty() &&
                projectVersion != null && !projectVersion.isEmpty()) {
            return true;
        } else {
            return false;
        }
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
        projectId = SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, getProjectIdParameter());

        projectName = SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, getProjectNameParameter());

        projectVersion = SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, getProjectVersionParameter());
    }

    @Override
    Object execute(SW360Connection connection) {
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

    @Override
    String[] printResult() {
        return result.stream()
                .map(SW360SparseRelease::csvPrintRow)
                .toArray(String[]::new);
    }

    @Override
    String getResultFileHeader() {
        return result.stream()
                .findFirst()
                .map(SW360SparseRelease::csvPrintHeader)
                .orElse("");
    }

    public String getProjectIdParameter() {
        return PROJECT_ID;
    }

    public String getProjectNameParameter() {
        return PROJECT_NAME;
    }

    public String getProjectVersionParameter() {
        return PROJECT_VERSION;
    }
}