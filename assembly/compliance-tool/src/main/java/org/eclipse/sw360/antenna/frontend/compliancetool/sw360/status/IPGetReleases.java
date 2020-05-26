package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import java.util.Collections;
import java.util.Set;

public class IPGetReleases extends InfoParameter {
    private static final String GET_RELEASES_CLEARED = SW360StatusReporterParameters.REPORTER_PARAMETER_PREFIX + "releases-cleared";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_CLEARED;
    }

    @Override
    boolean hasAdditionalParameters() {
        return false;
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
        return Collections.emptySet();
    }

    @Override
    void execute() {

    }
}
