/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.jsonreader;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.eclipse.sw360.antenna.model.xml.generated.*;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a JsonDocument and returns its JsonObjects.
 */
public class JsonReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonReader.class);
    private static final String COMPONENTS = "components";
    private final Charset encoding;
    private final Path recordingFile;
    private final Path dependencyDir;

    private static final String JSON_OBJ_PATHNAMES = "pathnames";
    private static final String JSON_OBJ_VERSION = "version";
    private static final String JSON_OBJ_COORDINATES = "coordinates";

    private static final Map<String, MissingLicenseReasons> SPECIAL_INFORMATION = Stream.of(new Object[][] {
                {"No-Sources", MissingLicenseReasons.NO_SOURCES},
                {"No-Source-License", MissingLicenseReasons.NO_LICENSE_IN_SOURCES},
                {"Not-Declared", MissingLicenseReasons.NOT_DECLARED},
                {"Not-Provided", MissingLicenseReasons.NOT_PROVIDED},
                {"Not-Supported", MissingLicenseReasons.NOT_SUPPORTED},
                {"Non-Standard", MissingLicenseReasons.NON_STANDARD}})
                .collect(Collectors.toMap(data -> (String) data[0], data -> (MissingLicenseReasons) data[1]));

    public JsonReader(Path recordingFile, Path dependencyDir, Charset encoding) {
        this.recordingFile = recordingFile;
        this.encoding = encoding;
        this.dependencyDir = dependencyDir;
    }

    private List<JsonObject> readJsonObjects(InputStream stream) {
        List<JsonObject> artifactList = new ArrayList<>();
        try (InputStream recordingStream = new RecordingInputStream(stream, recordingFile);
             InputStreamReader reader = new InputStreamReader(recordingStream, encoding)) {
            Object obj = Jsoner.deserialize(reader);
            JsonObject temp = (JsonObject) obj;
            JsonArray data = (JsonArray) temp.get(COMPONENTS);
            for (Object aData : data) {
                JsonObject jsonObject = (JsonObject) aData;
                artifactList.add(jsonObject);
            }
            stream.close();
            return artifactList;
        } catch (IOException | JsonException e) {
            throw new ExecutionException("Cannot read json objects from input stream", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected List<Artifact> createArtifactsList(InputStream stream, List<String> filterStrings) {
        LOGGER.debug("Create artifacts list from input stream.");
        List<Artifact> artifacts = new ArrayList<>();
        List<JsonObject> objects = readJsonObjects(stream);
        for (JsonObject obj : objects) {
            if (filterObject(obj, Optional.ofNullable(filterStrings))) {
                continue;
            }
            Artifact artifact = mapArtifact(obj);
            artifacts.add(artifact);
        }
        LOGGER.debug("Creation of artifacts list finished.");
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.debug("Failed to close stream. Ignored.");
        }
        return artifacts;
    }

    public List<Artifact> createArtifactsList(InputStream is) {
        return createArtifactsList(is, null);
    }

    private boolean filterObject(JsonObject object, Optional<List<String>> filterStrings) {
        // Returns true if this object should be filtered (i.e. not included
        // in the results
        JsonArray pathnames = (JsonArray) object.get(JSON_OBJ_PATHNAMES);
        return pathnames != null &&
                !pathnames.isEmpty() &&
                filterStrings.isPresent() &&
                filterStrings.get().stream()
                        .anyMatch(x -> pathnames.get(0).equals(x));
    }

    /**
     * Maps JsonObject to Antenna artifact.
     */
    private Artifact mapArtifact(JsonObject obj) {
        JsonObject licenseDataObj = (JsonObject) obj.get("licenseData");
        JsonObject securityDataObj = (JsonObject) obj.get("securityData");
        Artifact a = new Artifact("JSON")
                .addFact(mapFilename(obj))
                .addFact(new ArtifactPathnames(mapPathNames(obj)))
                .addFact(new ArtifactMatchingMetadata(mapMatchState(obj)))
                .addFact(new DeclaredLicenseInformation(mapLicenses("declaredLicenses", licenseDataObj)))
                .addFact(new ObservedLicenseInformation(mapLicenses("observedLicenses", licenseDataObj)))
                .addFact(new OverriddenLicenseInformation(mapLicenses("overriddenLicenses", licenseDataObj)))
                .addFact(new ArtifactIssues(mapSecurityIssues(securityDataObj)))
                .setProprietary(mapProprietary(obj))
                .addFact(new ArtifactSourceUrl(mapArtifactDownloadurl(obj)));
        mapCoordinates(obj).ifPresent(a::addCoordinate);
        potentiallyAddSpecialLicenseInformation(licenseDataObj, a);
        return a;
    }

    private void potentiallyAddSpecialLicenseInformation(JsonObject licenseDataObj, Artifact a) {
        List<MissingLicenseReasons> missingLicenseReasons = new ArrayList<>();
        missingLicenseReasons.addAll(extractSpecialLicenseDeclarations("declaredLicenses", licenseDataObj));
        missingLicenseReasons.addAll(extractSpecialLicenseDeclarations("observedLicenses", licenseDataObj));
        missingLicenseReasons.addAll(extractSpecialLicenseDeclarations("overriddenLicenses", licenseDataObj));
        if (!missingLicenseReasons.isEmpty()) {
            a.addFact(new MissingLicenseInformation(missingLicenseReasons));
        }
    }

    private List<MissingLicenseReasons> extractSpecialLicenseDeclarations(String identifier, JsonObject licenseDataObj) {
        List<MissingLicenseReasons> missingLicenseReasons = new ArrayList<>();
        if (null != licenseDataObj) {
            JsonArray objs = (JsonArray) licenseDataObj.get(identifier);
            if (null != objs) {
                Spliterator<Object> tmp = objs.spliterator();
                StreamSupport.stream(tmp, false)
                        .map(obj -> (JsonObject) obj)
                        .map(obj -> (String) obj.get("licenseId"))
                        .filter(SPECIAL_INFORMATION::containsKey)
                        .forEach(specialLicenseInformation -> missingLicenseReasons.add(SPECIAL_INFORMATION.get(specialLicenseInformation)));
                return missingLicenseReasons;
            }
        }
        return missingLicenseReasons;
    }

    private Issues mapSecurityIssues(JsonObject securityDataObj) {
        Issues issues = new Issues();
        if (securityDataObj != null) {
            JsonArray securityIssueObjs = (JsonArray) securityDataObj.get("securityIssues");
            if (securityIssueObjs != null) {
                for (Object securityIssueObj : securityIssueObjs) {
                    JsonObject json = (JsonObject) securityIssueObj;
                    Issue issue = new Issue();
                    issue.setReference((String) json.get("reference"));
                    issue.setSeverity(parseSeverity(json));
                    issue.setSource((String) json.get("source"));
                    issue.setStatus(SecurityIssueStatus.fromValue((String) json.getOrDefault("status", "Open")));
                    issue.setUrl((String) json.get("url"));
                    issues.getIssue().add(issue);
                }
            }
        }
        return issues;
    }

    private static double parseSeverity(JsonObject json) {
        Object o = json.get("severity");
        if (o == null) {
            return 10.0;
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException ex) {
            return 10.0;
        }
    }

    private ArtifactFilename mapFilename(JsonObject obj) {
        final String hash = (String) obj.get("hash");
        if(obj.containsKey(JSON_OBJ_PATHNAMES)) {
            final JsonArray pathnames = (JsonArray) obj.get(JSON_OBJ_PATHNAMES);
            if (pathnames.size() == 1) {
                String filename = Optional.ofNullable(Paths.get(pathnames.getString(0)).getFileName())
                        .orElseThrow(() -> new ExecutionException("Getting Path of [" + pathnames.getString(0) + "] returned null"))
                        .toString();
                return new ArtifactFilename(filename, hash);
            }
        }
        return new ArtifactFilename(null, hash);
    }

    private LicenseInformation mapLicenses(String identifier, JsonObject licenseDataObj) {
        if (null != licenseDataObj) {
            JsonArray objs = (JsonArray) licenseDataObj.get(identifier);
            if (null != objs) {
                Spliterator<Object> tmp = objs.spliterator();
                Collection<String> licenses = StreamSupport.stream(tmp, false)
                        .map(obj -> (JsonObject) obj)
                        .map(obj -> (String) obj.get("licenseId"))
                        // We delete all special strings which can be used to convey information about missing licenses
                        .filter(licenseId -> !SPECIAL_INFORMATION.containsKey(licenseId))
                        .collect(Collectors.toSet());
                return LicenseSupport.mapLicenses(licenses);
            }
        }
        return new LicenseStatement();
    }

    private String[] mapPathNames(JsonObject obj) {
        JsonArray jPathNames = (JsonArray) obj.get(JSON_OBJ_PATHNAMES);
        if (jPathNames == null) {
            return new String[]{};
        }

        Stream<String> targetStream = jPathNames.stream().map(Object::toString);
        return targetStream.map(Paths::get).map(path -> {
            if (!path.isAbsolute()) {
                return dependencyDir.resolve(path).toString();
            } else {
                return path.toString();
            }
        }).toArray(String[]::new);
    }

    private boolean mapProprietary(JsonObject obj) {
        Boolean ppObject = (Boolean) obj.get("proprietary");
        return null != ppObject && ppObject;
    }

    private MatchState mapMatchState(JsonObject obj) {
        String ms = (String) obj.get("matchState");
        return MatchState.valueOf(ms.toUpperCase());
    }

    private Optional<Coordinate> mapMavenCoordinates(JsonObject objCoordinates) {
        if (null != objCoordinates) {
            return Optional.of(new Coordinate(
                    Coordinate.Types.MAVEN,
                    (String) objCoordinates.get("groupId"),
                    (String) objCoordinates.get("artifactId"),
                    (String) objCoordinates.get(JSON_OBJ_VERSION)));
        }
        return Optional.empty();
    }

    private Optional<Coordinate> mapJavaScriptCoordinates(JsonObject objCoordinates) {
        if (objCoordinates != null) {
            return Optional.of(new Coordinate(
                    Coordinate.Types.NPM,
                    getJavaScriptCoordinatesNamespace((String) objCoordinates.get("name")),
                    getJavaScriptCoordinatesPackageName((String) objCoordinates.get("name")),
                    (String) objCoordinates.get(JSON_OBJ_VERSION)));
        }
        return Optional.empty();
    }

    private Optional<Coordinate> mapDotNetCoordinates(JsonObject objCoordinates) {
        if (objCoordinates != null) {
            return Optional.of(new Coordinate(
                    Coordinate.Types.NUGET,
                    (String) objCoordinates.get("packageId"),
                    (String) objCoordinates.get(JSON_OBJ_VERSION)));
        }
        return Optional.empty();
    }

    private Optional<Coordinate> mapNpmCoordinates(JsonObject objCoordinates) {
        if (objCoordinates != null) {
            return Optional.of(new Coordinate(
                    Coordinate.Types.NPM,
                    (String) objCoordinates.get("packageId"),
                    (String) objCoordinates.get(JSON_OBJ_VERSION)
            ));
        }
        return Optional.empty();
    }

    private Optional<Coordinate> mapCoordinates(JsonObject object) {
        JsonObject objComponentIdentifier = (JsonObject) object.get("componentIdentifier");
        if (objComponentIdentifier != null) {
            String format = (String) objComponentIdentifier.get("format");
            switch (format) {
                case "a-name":
                    return mapJavaScriptCoordinates((JsonObject) objComponentIdentifier.get(JSON_OBJ_COORDINATES));
                case "maven":
                    return mapMavenCoordinates((JsonObject) objComponentIdentifier.get(JSON_OBJ_COORDINATES));
                case "npm":
                    return mapNpmCoordinates((JsonObject) objComponentIdentifier.get(JSON_OBJ_COORDINATES));
                case "nuget":
                    return mapDotNetCoordinates((JsonObject) objComponentIdentifier.get(JSON_OBJ_COORDINATES));
            }
        }
        return Optional.empty();
    }

    private String getJavaScriptCoordinatesNamespace(String name) {
        String[] nameParts = name.split("/");

        if (nameParts.length > 1 && nameParts[0].startsWith("@")) {
            return nameParts[0];
        }
        return null;
    }

    private String getJavaScriptCoordinatesPackageName(String name) {
        String[] nameParts = name.split("/");

        if (nameParts.length > 1 && nameParts[0].startsWith("@")) {
            return Arrays.stream(nameParts)
                    .skip(1)
                    .collect(Collectors.joining("/"));
        }
        return name;
    }

    private String mapArtifactDownloadurl(JsonObject obj) {
        return (String) obj.get("downloadurl");
    }

    protected Charset getEncoding() {
        return encoding;
    }

    protected Path getRecordingFile() {
        return recordingFile;
    }
}
