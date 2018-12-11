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

package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
 * Reads a JsonDocument and returns its JSONObjects.
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

    private List<JSONObject> readJsonObjects(InputStream stream) {
        List<JSONObject> artifactList = new ArrayList<>();
        try (InputStream recordingStream = new RecordingInputStream(stream, recordingFile);
             InputStreamReader reader = new InputStreamReader(recordingStream, encoding)) {
            Object obj = JSONValue.parse(reader);
            JSONObject temp = (JSONObject) obj;
            JSONArray data = (JSONArray) temp.get(COMPONENTS);
            for (Object aData : data) {
                JSONObject jsonObject = (JSONObject) aData;
                artifactList.add(jsonObject);
            }
            stream.close();
            return artifactList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Artifact> createArtifactsList(InputStream stream, List<String> filterStrings) {
        LOGGER.debug("Create artifacts list from input stream.");
        List<Artifact> artifacts = new ArrayList<>();
        List<JSONObject> objects = readJsonObjects(stream);
        for (JSONObject obj : objects) {
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

    private boolean filterObject(JSONObject object, Optional<List<String>> filterStrings) {
        // Returns true if this object should be filtered (i.e. not included
        // in the results
        JSONArray pathnames = (JSONArray) object.get("pathnames");
        return pathnames != null &&
                pathnames.size() > 0 &&
                filterStrings.isPresent() &&
                filterStrings.get().stream()
                        .anyMatch(x -> pathnames.get(0).equals(x));
    }

    /**
     * Maps JSONObject to Antenna artifact.
     */
    private Artifact mapArtifact(JSONObject obj) {
        JSONObject licenseDataObj = (JSONObject) obj.get("licenseData");
        JSONObject securityDataObj = (JSONObject) obj.get("securityData");
        Artifact a = new Artifact("JSON")
                .addFact(new ArtifactFilename(null, (String) obj.get("hash")))
                .addFact(new ArtifactPathnames(mapPathNames(obj)))
                .addFact(new ArtifactMatchingMetadata(mapMatchState(obj)))
                .addFact(new DeclaredLicenseInformation(mapLicenses("declaredLicenses", licenseDataObj)))
                .addFact(new ObservedLicenseInformation(mapLicenses("observedLicenses", licenseDataObj)))
                .addFact(new OverriddenLicenseInformation(mapLicenses("overriddenLicenses", licenseDataObj)))
                .addFact(new ArtifactIssues(mapSecurityIssues(securityDataObj)))
                .setProprietary(mapProprietary(obj));
        mapCoordinates(obj).ifPresent(a::addFact);
        return a;
    }

    private Issues mapSecurityIssues(JSONObject securityDataObj) {
        Issues issues = new Issues();
        if ( securityDataObj != null ) {
            JSONArray securityIssueObjs = (JSONArray) securityDataObj.get("securityIssues");
            if (securityIssueObjs != null) {
                for (Object securityIssueObj : securityIssueObjs) {
                    JSONObject json = (JSONObject) securityIssueObj;
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

    private static double parseSeverity( JSONObject json ) {

        Object o = json.get( "severity" );
        if ( o instanceof Double ) {
            return (Double) o;
        }
        return 10.0;
    }

    private LicenseInformation mapLicenses(String identifier, JSONObject licenseDataObj) {
        if (null != licenseDataObj) {
            JSONArray objs = (JSONArray) licenseDataObj.get(identifier);
            if (null != objs) {
                Spliterator<JSONObject> tmp = objs.spliterator();
                Collection<String> licenses = StreamSupport.stream(tmp, false)
                        .map(obj -> (String) obj.get("licenseId"))
                        .collect(Collectors.toSet());
                return LicenseSupport.mapLicenses(licenses);
            }
        }
        return new LicenseStatement();
    }

    private String[] mapPathNames(JSONObject obj) {
        JSONArray jPathNames = (JSONArray) obj.get("pathnames");
        if (jPathNames == null) {
            return new String[]{};
        }

        Stream<String> targetStream = StreamSupport.stream(jPathNames.spliterator(), false);
        return targetStream.map(path -> Paths.get(path)).map(path -> {
            if (!path.isAbsolute()) {
                return dependencyDir.resolve(path).toString();
            } else {
                return path.toString();
            }
        }).toArray(String[]::new);
    }

    private boolean mapProprietary(JSONObject obj) {
        Boolean ppObject = (Boolean) obj.get("proprietary");
        return (null != ppObject && ppObject);
    }

    private MatchState mapMatchState(JSONObject obj) {
        String ms = (String) obj.get("matchState");
        return MatchState.valueOf(ms.toUpperCase());
    }

    private Optional<ArtifactCoordinates> mapMavenCoordinates(JSONObject objCoordinates) {
        if (null != objCoordinates) {
            MavenCoordinates.MavenCoordinatesBuilder c = new MavenCoordinates.MavenCoordinatesBuilder();
            c.setGroupId((String) objCoordinates.get("groupId"));
            c.setArtifactId((String) objCoordinates.get("artifactId"));
            c.setVersion((String) objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }

    private Optional<ArtifactCoordinates> mapJavaScriptCoordinates(JSONObject objCoordinates) {
        if (objCoordinates != null) {
            JavaScriptCoordinates.JavaScriptCoordinatesBuilder c = new JavaScriptCoordinates.JavaScriptCoordinatesBuilder();
            c.setName((String) objCoordinates.get("name"));
            c.setVersion((String) objCoordinates.get("version"));
            c.setArtifactId(objCoordinates.get("name") + "-" + objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }

    private Optional<ArtifactCoordinates> mapDotNetCoordinates(JSONObject objCoordinates) {
        if (objCoordinates != null) {
            DotNetCoordinates.DotNetCoordinatesBuilder c = new DotNetCoordinates.DotNetCoordinatesBuilder();
            c.setPackageId((String) objCoordinates.get("packageId"));
            c.setVersion((String) objCoordinates.get("version"));
            return Optional.of(c.build());
        }
        return Optional.empty();
    }


    private Optional<ArtifactCoordinates> mapCoordinates(JSONObject object) {
        JSONObject objComponentIdentifier = (JSONObject) object.get("componentIdentifier");
        if (objComponentIdentifier != null) {
            String format = (String) objComponentIdentifier.get("format");
            switch (format) {
                case "a-name": return mapJavaScriptCoordinates((JSONObject) objComponentIdentifier.get("coordinates"));
                case "maven": return mapMavenCoordinates((JSONObject) objComponentIdentifier.get("coordinates"));
                case "nuget": return mapDotNetCoordinates((JSONObject) objComponentIdentifier.get("coordinates"));
            }
        }
        return Optional.empty();
    }
}
