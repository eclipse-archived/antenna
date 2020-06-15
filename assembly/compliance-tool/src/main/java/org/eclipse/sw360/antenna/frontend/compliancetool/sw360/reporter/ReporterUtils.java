/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;

class ReporterUtils {
    private ReporterUtils() {
    }

    static String[][] printCollectionOfReleases(Collection<SW360Release> releases) {
        return releases.stream()
                .map(ReporterUtils::releaseCsvPrintRow)
                .toArray(String[][]::new);
    }

    static String releaseCsvPrintHeader(String delimiter) {
        return "release id" + delimiter + "name" + delimiter + "version" + delimiter +
                "coordinates" + delimiter + "main license ids" + delimiter +
                "overridden license" + delimiter + " declared license" + delimiter +
                "observed license" + delimiter + "copyrights" + delimiter +
                "clearing state" + delimiter + "change status" + delimiter +
                "download url" + delimiter + "release tag url" + delimiter + "software heritage id";
    }

    private static String[] releaseCsvPrintRow(SW360Release release) {
        return new String[]{
                release.getId(), release.getName(), release.getVersion(),
                collectionToString(release.getCoordinates().values()),
                collectionToString(release.getMainLicenseIds()),
                release.getOverriddenLicense(), release.getDeclaredLicense(), release.getObservedLicense(),
                release.getCopyrights(), release.getClearingState(), release.getChangeStatus(),
                release.getDownloadurl(), release.getReleaseTagUrl(), release.getSoftwareHeritageId()};
    }

    static String[][] printCollectionOfSparseReleases(Collection<SW360SparseRelease> releases) {
        return releases.stream()
                .map(ReporterUtils::sparseReleaseCsvPrintRow)
                .toArray(String[][]::new);
    }

    static String sparseReleaseCsvPrintHeader(String delimiter) {
        return "releaseId" + delimiter + "name" + delimiter + "version" +
                delimiter + "mainLicenseIds" + delimiter + "componentId";
    }

    private static String[] sparseReleaseCsvPrintRow(SW360SparseRelease release) {
        return new String[]{release.getReleaseId(), release.getName(),
                release.getVersion(), collectionToString(release.getMainLicenseIds()), release.getComponentId()};
    }

    private static String collectionToString(Collection<String> collection) {
        if (collection == null) {
            return "";
        }
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

