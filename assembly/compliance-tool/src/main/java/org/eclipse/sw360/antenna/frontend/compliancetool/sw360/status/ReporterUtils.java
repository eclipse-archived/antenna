package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collection;

public class ReporterUtils {
    public static String[] printCollectionOfReleases(Collection<SW360Release> releases) {
        return releases.stream()
                .map(SW360Release::csvPrintRow)
                .toArray(String[]::new);
    }
}

