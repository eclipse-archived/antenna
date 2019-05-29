/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
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
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
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
    protected final Charset encoding;
    protected final Path recordingFile;
    private final Path dependencyDir;

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
            throw new RuntimeException(e);
        }
    }

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
            // ignore
        }
        return artifacts;
    }

    public List<Artifact> createArtifactsList(InputStream is) {
        return createArtifactsList(is, null);
    }

    private boolean filterObject(JsonObject object, Optional<List<String>> filterStrings) {
        // Returns true if this object should be filtered (i.e. not included
        // in the results
        JsonArray pathnames = (JsonArray) object.get("pathnames");
        return pathnames != null &&
                pathnames.size() > 0 &&
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
                .setProprietary(mapProprietary(obj));
        mapCoordinates(obj).ifPresent(a::addFact);
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
                        .filter(SpecialLicenseInformation.SPECIAL_INFORMATION::containsKey)
                        .forEach(specialLicenseInformation -> missingLicenseReasons.add(SpecialLicenseInformation.SPECIAL_INFORMATION.get(specialLicenseInformation)));
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
        if(obj.containsKey("pathnames")) {
            final JsonArray pathnames = (JsonArray) obj.get("pathnames");
            if (pathnames.size() == 1) {
                String filename = Paths.get(pathnames.getString(0)).getFileName().toString();
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
                        .filter(licenseId -> !SpecialLicenseInformation.SPECIAL_INFORMATION.containsKey(licenseId))
                        .collect(Collectors.toSet());
                return LicenseSupport.mapLicenses(licenses);
            }
        }
        return new LicenseStatement();
    }

    private String[] mapPathNames(JsonObject obj) {
        JsonArray jPathNames = (JsonArray) obj.get("pathnames");
        if (jPathNames == null) {
            return new String[]{};
        }

        Stream<String> targetStream = jPathNames.stream().map(Object::toString);
        return targetStream.map(path -> Paths.get(path)).map(path -> {
            if (!path.isAbsolute()) {
                return dependencyDir.resolve(path).toString();
            } else {
                return path.toString();
            }
        }).toArray(String[]::new);
    }

    private boolean mapProprietary(JsonObject obj) {
        Boolean ppObject = (Boolean) obj.get("proprietary");
        return (null != ppObject && ppObject);
    }

    private MatchState mapMatchState(JsonObject obj) {
        String ms = (String) obj.get("matchState");
        return MatchState.valueOf(ms.toUpperCase());
    }

    private Optional<ArtifactCoordinates> mapMavenCoordinates(JsonObject objCoordinates) {
        if (null != objCoordinates) {
            MavenCoordinates.MavenCoordinatesBuilder c = new MavenCoordinates.MavenCoordinatesBuilder();
            c.setGroupId((String) objCoordinates.get("groupId"));
            c.setArtifactId((String) objCoordinates.get("artifactId"));
            c.setVersion((String) objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }

    private Optional<ArtifactCoordinates> mapJavaScriptCoordinates(JsonObject objCoordinates) {
        if (objCoordinates != null) {
            JavaScriptCoordinates.JavaScriptCoordinatesBuilder c = new JavaScriptCoordinates.JavaScriptCoordinatesBuilder();
            c.setName((String) objCoordinates.get("name"));
            c.setVersion((String) objCoordinates.get("version"));
            c.setArtifactId(objCoordinates.get("name") + "-" + objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }

    private Optional<ArtifactCoordinates> mapDotNetCoordinates(JsonObject objCoordinates) {
        if (objCoordinates != null) {
            DotNetCoordinates.DotNetCoordinatesBuilder c = new DotNetCoordinates.DotNetCoordinatesBuilder();
            c.setPackageId((String) objCoordinates.get("packageId"));
            c.setVersion((String) objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }

    private Optional<ArtifactCoordinates> mapCoordinates(JsonObject object) {
        JsonObject objComponentIdentifier = (JsonObject) object.get("componentIdentifier");
        if (objComponentIdentifier != null) {
            String format = (String) objComponentIdentifier.get("format");
            switch (format) {
                case "a-name":
                    return mapJavaScriptCoordinates((JsonObject) objComponentIdentifier.get("coordinates"));
                case "maven":
                    return mapMavenCoordinates((JsonObject) objComponentIdentifier.get("coordinates"));
                case "nuget":
                    return mapDotNetCoordinates((JsonObject) objComponentIdentifier.get("coordinates"));
            }
        }
        return Optional.empty();
    }
}
