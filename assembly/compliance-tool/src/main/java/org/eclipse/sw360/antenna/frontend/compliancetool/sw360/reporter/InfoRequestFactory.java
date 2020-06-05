package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.main.AntennaComplianceToolOptions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * A factory class for creating a new {@link InfoRequest}.
 * </p>
 * <p>
 * This class an create an {@link InfoRequest} object from a {@link String}.
 * From this an info parameter can be obtained that can be used to
 * obtain the needed information.
 * </p>
 */
public class InfoRequestFactory {
    private static Set<InfoRequest> infoRequests = new HashSet<>(Arrays.asList(
            new IRGetClearedReleases(),
            new IRGetReleasesOfProjects(),
            new IRGetNotClearedReleases()
    )) ;

    /**
     * Gives {@code InfoRequest} from a given parameter string
     * @param infoParameter String containing parameter
     * @return implementation of an InfoRequest or an emptyInfoRequest
     */
    static InfoRequest getInfoRequestFromString(String infoParameter) {
        return infoRequests.stream()
                .filter(ir -> ir.getInfoParameter().equalsIgnoreCase(infoParameter))
                .findFirst()
                .orElse(InfoRequest.emptyInfoRequest());
    }

    /**
     * Returns a help message that describes the parameter options supported
     * by the status reporter.
     *
     * @return the help message
     */
    static String helpMessage() {
        String cr = System.lineSeparator();
        String infoParameterString = infoRequests.stream()
                .map(InfoRequest::getInfoParameter)
                .collect(Collectors.joining(cr));
        return "Usage: java -jar compliancetool.jar " + AntennaComplianceToolOptions.SWITCH_REPORTER + "[options] <complianceMode> <propertiesFilePath>" + cr + cr +
                "The reporter create a csv file for every run with the requested information statement. " + cr + cr +
                "The reporter info statements: (only one can be set)" + cr +
                infoParameterString;
    }
}
