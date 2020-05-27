package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.frontend.compliancetool.main.AntennaComplianceToolOptions;
import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

public class SW360StatusReporterParameters {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360StatusReporterParameters.class);

    public static final String REPORTER_PARAMETER_PREFIX = "--info=";

    public static InfoParameter getInfoRequestFromParameter(Set<String> parameters) {
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

    private static InfoParameter parse(Set<String> parameters) {
        final Optional<String> infoParameter = parameters.stream().filter(p -> p.contains(REPORTER_PARAMETER_PREFIX)).findFirst();
        final InfoParameter infoParameter1 = infoParameter.map(SW360StatusReporterParameters::getInfoParameterFromString).orElse(InfoParameter.emptyInfoParameter());

        if (infoParameter1 == InfoParameter.emptyInfoParameter()) {
            throw new IllegalArgumentException(infoParameter.get() + ": " + infoParameter1.helpMessage());
        }

        if (infoParameter1.hasAdditionalParameters()) {
            infoParameter1.parseAdditionalParameter(parameters);
        } else if (parameters.size() < 1) {
            LOGGER.warn("You have provided additional parameters that are not necessary for the information parameter {}.", infoParameter1.getInfoParameter());
        }

        if (infoParameter1.isValid()) {
            return infoParameter1;
        } else {
            throw new IllegalStateException(
                    "The information parameter " + infoParameter1.getInfoParameter() + " you requested does not have all parameters it needs." +
                            System.lineSeparator() + infoParameter1.helpMessage());
        }
    }

    private static InfoParameter getInfoParameterFromString(String infoParameter) {
        switch (infoParameter) {
           case "--info=releases-cleared":
                return new IPGetClearedReleases();
            case "--info=releases-of-project":
                return new IPGetReleasesOfProjects();
            case "--info=releases-not-cleared":
                return new IPGetNotClearedReleases();
            default:
                return InfoParameter.emptyInfoParameter();
        }
    }

    private static String getParameterValueFromParameter(String parameter) {
        String[] parameterParts = parameter.split(AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER);
        if (parameterParts.length != 2) {
            throw new IllegalArgumentException (
                    "The provided parameter " + parameter + " does not adhere to the structure --<parameter>=<value> and is therefore invalid.");
        }
        return parameterParts[1];
    }

    private static String getParameterKeyFromParameter(String parameter) {
        String[] parameterParts = parameter.split(AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER);
        if (parameterParts.length != 2) {
            throw new IllegalArgumentException (
                    "The provided parameter " + parameter + " does not adhere to the structure --<parameter>=<value> and is therefore invalid.");
        }
        return parameterParts[0];
    }

    public static String parseParameterValueFromListOfParameters(Set<String> parameters, String parameterName) {
        return parameters.stream()
                .filter(p -> getParameterKeyFromParameter(p).equals(parameterName))
                .findFirst()
                .map(SW360StatusReporterParameters::getParameterValueFromParameter)
                .orElse(null);
    }

    /**
     * Returns a help message that describes the parameter options supported
     * by the status reporter.
     *
     * @return the help message
     */
    static String helpMessage() {
        String cr = System.lineSeparator();
        return "Usage: java -jar compliancetool.jar " + AntennaComplianceToolOptions.SWITCH_REPORTER + "[options] <complianceMode> <propertiesFilePath>" + cr + cr +
                "Supported options:" + cr +
                AntennaComplianceToolOptions.SWITCH_HELP_SHORT + ", " + AntennaComplianceToolOptions.SWITCH_HELP_LONG + ":    Displays this help message." + cr +
                AntennaComplianceToolOptions.SWITCH_DEBUG_SHORT + ", " + AntennaComplianceToolOptions.SWITCH_DEBUG_LONG +
                ":   Sets log level to DEBUG for diagnostic purposes." + cr + cr +
                "The reporter create a csv file for every run with the requested information statement. " + cr + cr +
                "The reporter info statements: (only one can be set)" + cr +
                new IPGetClearedReleases().getInfoParameter() + ":   Gives a list of all releases in a given sw360 instances that are cleared." + cr;
    }
}
