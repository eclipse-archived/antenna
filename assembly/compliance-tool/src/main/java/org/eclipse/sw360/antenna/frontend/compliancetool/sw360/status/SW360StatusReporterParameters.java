package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.frontend.compliancetool.main.AntennaComplianceToolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SW360StatusReporterParameters {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360StatusReporterParameters.class);

    private static final String REPORTER_PARAMETER_PREFIX = "--info=";

    public InfoParameter getInfoRequestFromParameter(Set<String> parameters) {
        if (parameters.isEmpty()) {
            LOGGER.error("No parameters provided for the status reporter.");
            LOGGER.info(helpMessage());
            throw new IllegalArgumentException("No parameters provided for the status reporter.");
        }
        if (parameters.stream().filter(p -> p.contains(REPORTER_PARAMETER_PREFIX)).count() != 1) {
            LOGGER.error("Too many information requests were made in this status report. ");
            LOGGER.info(helpMessage());
            throw new IllegalArgumentException("Too many information requests were made in this status report. ");
        }
        return parse(parameters);
    }

    private InfoParameter parse(Set<String> parameters) {
        final Optional<String> infoParameter = parameters.stream().filter(p -> p.contains(REPORTER_PARAMETER_PREFIX)).findFirst();
        final Optional<InfoParameter> optionalInfoParameter = infoParameter.map(this::getInfoParameterFromString);

        return optionalInfoParameter.orElse(null);
    }

    private InfoParameter getInfoParameterFromString (String infoParameter) {
        InfoParameter infoParam;
        switch (infoParameter) {
            case new GetReleases().getInfoParameter():
                return new GetReleases();
            case new GetReleasesOfProjects().getInfoParameter():
                return new GetReleasesOfProjects();
            default:
                return emptyInfoParameter();


        }
    }

    /**
     * Returns a help message that describes the parameter options supported
     * by the status reporter.
     *
     * @return the help message
     */
    String helpMessage() {
        String cr = System.lineSeparator();
        return "Usage: java -jar compliancetool.jar " + AntennaComplianceToolOptions.SWITCH_REPORTER + "[options] <complianceMode> <propertiesFilePath>" + cr + cr +
                "Supported options:" + cr +
                AntennaComplianceToolOptions.SWITCH_HELP_SHORT + ", " + AntennaComplianceToolOptions.SWITCH_HELP_LONG + ":    Displays this help message." + cr +
                AntennaComplianceToolOptions.SWITCH_DEBUG_SHORT + ", " + AntennaComplianceToolOptions.SWITCH_DEBUG_LONG +
                ":   Sets log level to DEBUG for diagnostic purposes." + cr + cr +
                "The reporter create a csv file for every run with the requested information statement. " + cr + cr +
                "The reporter info statements: (only one can be set)" + cr +
                new GetReleases().getInfoParameter() + ":   Gives a list of all releases in a given sw360 instances that are cleared." + cr;
    }

    abstract class InfoParameter {
        Set<String> parameters;
        abstract String getInfoParameter();
        abstract boolean hasAdditionalParameters();
        abstract String helpMessage();
        abstract boolean isValid();
        abstract Set<String> getAdditionalParameters();
    }

    InfoParameter emptyInfoParameter() {
        return new InfoParameter() {
            @Override
            String getInfoParameter() {
                return "NON_VALID";
            }

            @Override
            boolean hasAdditionalParameters() {
                return false;
            }

            @Override
            String helpMessage() {
                return "The provided info parameter is not supported in this status reporter";
            }

            @Override
            boolean isValid() {
                return false;
            }

            @Override
            Set<String> getAdditionalParameters() {
                return null;
            }
        };
    }

    class GetReleases extends InfoParameter{
        static final String GET_RELEASES_CLEARED = REPORTER_PARAMETER_PREFIX + "releases-cleared";

        @Override
        static String getInfoParameter() {
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
    }

    class GetReleasesOfProjects extends InfoParameter {
        static final String GET_RELEASES_OF_PROJECT = REPORTER_PARAMETER_PREFIX + "releases-of-project";
        static final String PROJECT_NAME = "--project_name";
        static final String PROJECT_ID = "--project_id";

        @Override
        String getInfoParameter() {
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

        public String getProjectId() {
            return PROJECT_ID;
        }

        public String getProjectName() {
            return PROJECT_NAME;
        }
    }
}
