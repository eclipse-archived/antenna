package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

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
    /**
     * Gives {@code InfoRequest} from a given parameter string
     * @param infoParameter String containing parameter
     * @return implementation of an InfoRequest or an emptyInfoRequest
     */
    static InfoRequest getInfoRequestFromString(String infoParameter) {
        switch (infoParameter) {
            case "--info=releases-cleared":
                return new IRGetClearedReleases();
            case "--info=releases-of-project":
                return new IRGetReleasesOfProjects();
            case "--info=releases-not-cleared":
                return new IRGetNotClearedReleases();
            default:
                return InfoRequest.emptyInfoRequest();
        }
    }
}
