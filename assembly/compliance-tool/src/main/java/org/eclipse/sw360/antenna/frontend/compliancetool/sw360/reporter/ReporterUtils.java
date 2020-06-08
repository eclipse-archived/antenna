package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;

class ReporterUtils {
    static String[] printCollectionOfReleases(Collection<SW360Release> releases) {
        return releases.stream()
                .map(ReporterUtils::releaseCsvPrintRow)
                .toArray(String[]::new);
    }

    static String releaseCsvPrintHeader() {
        return "release id;name;version;" +
                "coordinates;" +
                "main license ids;" +
                "overridden license;declared license;observed license;" +
                "copyrights;clearing state;change status;" +
                "download url; release tag url; software heritage id";
    }

    private static String releaseCsvPrintRow(SW360Release release) {
        return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
                release.getId(), release.getName(), release.getVersion(),
                collectionToString(release.getCoordinates().values()),
                collectionToString(release.getMainLicenseIds()),
                release.getOverriddenLicense(), release.getDeclaredLicense(), release.getObservedLicense(),
                release.getCopyrights(), release.getClearingState(), release.getChangeStatus(),
                release.getDownloadurl(), release.getReleaseTagUrl(), release.getSoftwareHeritageId());
    }

    static String[] printCollectionOfSparseReleases(Collection<SW360SparseRelease> releases) {
        return releases.stream()
                .map(ReporterUtils::sparseReleaseCsvPrintRow)
                .toArray(String[]::new);
    }

    static String sparseReleaseCsvPrintHeader() {
        return "releaseId;name;version;mainLicenseIds;componentId";
    }

    private static String sparseReleaseCsvPrintRow(SW360SparseRelease release) {
        return String.format("%s;%s;%s;%s;%s", release.getReleaseId(), release.getName(),
                release.getVersion(), collectionToString(release.getMainLicenseIds()), release.getComponentId());
    }

    private static String collectionToString(Collection<String> collection) {
        String separator = "";
        StringBuilder mainLicenses = new StringBuilder();
        for (String item : collection) {
            mainLicenses.append(separator);
            separator = ",";
            mainLicenses.append(item);
        }
        return mainLicenses.toString();
    }

}

