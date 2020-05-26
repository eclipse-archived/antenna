package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import java.util.HashSet;
import java.util.Set;

public class IPGetReleasesOfProjects extends InfoParameter {
    static final String GET_RELEASES_OF_PROJECT = SW360StatusReporterParameters.REPORTER_PARAMETER_PREFIX + "releases-of-project";
    static final String PROJECT_NAME = "--project_name";
    static final String PROJECT_ID = "--project_id";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_OF_PROJECT;
    }

    @Override
    boolean hasAdditionalParameters() {
        return true;
    }

    @Override
    String helpMessage() {
        return null;
    }

    @Override
    boolean isValid() {
        return false;
    }

    @Override
    Set<String> getAdditionalParameters() {
        Set<String> additionalParamaters = new HashSet<>();
        additionalParamaters.add(PROJECT_ID);
        additionalParamaters.add(PROJECT_NAME);
        return additionalParamaters;
    }

    @Override
    void execute() {

    }

    public String getProjectId() {
        return PROJECT_ID;
    }

    public String getProjectName() {
        return PROJECT_NAME;
    }
}