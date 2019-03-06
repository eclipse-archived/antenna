/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.p2;

import org.eclipse.equinox.p2.metadata.Version;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public final class ProjectArgumentExtractor {
    private static final String DOWNLOAD_AREA = "-download-area";
    private static final String REPOSITORIES = "-repositories";
    private static final String ARTIFACTS = "-coordinates";

    private ProjectArgumentExtractor() {
        // utility class
    }

    public static ProjectArguments extractArguments(List<String> arguments) throws P2Exception {
        ProjectArguments projectArguments = new ProjectArguments();
        Map<String, String> groupedArguments = groupByTwo(arguments);
        for (Map.Entry<String, String> entry : groupedArguments.entrySet()) {
            extractArgument(entry.getKey(), entry.getValue(), projectArguments);
        }
        return projectArguments;
    }

    private static Map<String, String> groupByTwo(List<String> arguments) throws P2Exception {
        List<String> allArguments = arguments.stream()
                .map(argument -> Arrays.asList(argument.split("\\s")))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return regroupArguments(allArguments);
    }

    private static Map<String, String> regroupArguments(List<String> allArguments) throws P2Exception {
        Map<String, String> argumentsList = new HashMap<>();
        if (allArguments.size() % 2 != 0) {
            throw new P2Exception("Arguments are incomplete, maybe too much whitespace?");
        }
        for (int i = 0; i < allArguments.size() / 2; i++) {
            String argumentKey = allArguments.get(2 * i);
            if (argumentKey.startsWith("-")) {
                argumentsList.put(argumentKey, allArguments.get(2*i + 1));
            } else {
                throw new P2Exception("Arguments must be of form '-descriptor some;arguments' but are " + argumentKey + " " + allArguments.get(2*i + 1));
            }
        }
        return argumentsList;
    }

    private static void extractArgument(String argumentKey, String argument, ProjectArguments projectArguments) throws P2Exception {
        if (argumentKey.equals(DOWNLOAD_AREA)) {
            extractDownloadArea(projectArguments, argument);
        } else if (argumentKey.equals(REPOSITORIES)) {
            extractRepositories(projectArguments, argument);
        } else if (argumentKey.equals(ARTIFACTS)) {
            extractArtifacts(projectArguments, argument);
        } else {
            throw new P2Exception("Unrecognized argument: " + argumentKey);
        }
    }

    private static void extractArtifacts(ProjectArguments projectArguments, String argument) throws P2Exception {
        String[] artifacts = argument.split(";");
        for (String artifactDescription : artifacts) {
            String[] coordinates = artifactDescription.split(",");
            if (coordinates.length != 2) {
                throw new P2Exception("Bundle coordinates need to be given in the form bundleSymbolicName,Version but was: " + artifactDescription);
            }
            Version osgiVersion = extractVersion(coordinates[1]);

            projectArguments.addArtifact(new P2Artifact(coordinates[0], osgiVersion));
        }
    }

    private static void extractRepositories(ProjectArguments projectArguments, String s) throws P2Exception {
        String[] repositories = s.split(";");
        for (String repository : repositories) {
            try {
                URI repositoryUri = new URI(repository);
                projectArguments.addRepository(repositoryUri);
            } catch (URISyntaxException e) {
                throw new P2Exception(REPOSITORIES + " should contain a list of semicolon separated URIs but is " + s);
            }
        }
    }

    private static void extractDownloadArea(ProjectArguments projectArguments, String pathname) throws P2Exception {
        File downloadArea = new File(pathname);
        if (!downloadArea.exists()) {
            throw new P2Exception("Download area does not yet exist at " + downloadArea.toString());
        }
        projectArguments.setDownloadArea(downloadArea);
    }

    private static Version extractVersion(String coordinate) throws P2Exception {
        String[] version = coordinate.split("\\.");
        if (version.length < 3) {
            throw new P2Exception("Bundle version needs to be given in the form major.minor.micro(.qualifier) but was: " + coordinate);
        }
        int major = extractSubversionElement(coordinate, version[0]);
        int minor = extractSubversionElement(coordinate, version[1]);
        int micro = extractSubversionElement(coordinate, version[2]);

        return version.length == 3
                ? Version.createOSGi(major, minor, micro)
                : Version.createOSGi(major, minor, micro, version[3]);
    }

    private static int extractSubversionElement(String coordinates, String version) throws P2Exception {
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException ex) {
            throw new P2Exception("Bundle version needs to be given in numeric form form major.minor.micro but was: " + coordinates);
        }
    }
}
