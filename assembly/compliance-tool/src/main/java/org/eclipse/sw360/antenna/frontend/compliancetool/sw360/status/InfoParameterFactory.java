package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

/**
 * <p>
 * A factory class for creating a new {@link InfoParameter}.
 * </p>
 * <p>
 * This class an create an {@link InfoParameter} object from a {@link String}.
 * From this an info parameter can be obtained that can be used to
 * obtain the needed information.
 * </p>
 */
public class InfoParameterFactory {
    /**
     * Gives {@code InfoParameter} from a given parameter string
     * @param infoParameter String containing parameter
     * @return implementation of an InfoParameter or an emptyInfoParameter
     */
    static InfoParameter getInfoParameterFromString(String infoParameter) {
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
}
