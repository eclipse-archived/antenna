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

import org.eclipse.sw360.antenna.model.Artifact;
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
            artifact.setAnalysisSource("JSON");
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
        Artifact artifact = new Artifact();
        artifact.setPathnames(mapPathNames(obj));
        artifact.setArtifactIdentifier(mapIdentifier(obj));
        artifact.setMatchState(mapMatchState(obj));
        artifact.setProprietary(mapProprietary(obj));
        JSONObject licenseDataObj = (JSONObject) obj.get("licenseData");
        artifact.setDeclaredLicenses(mapLicenses("declaredLicenses", licenseDataObj));
        artifact.setObservedLicenses(mapLicenses("observedLicenses", licenseDataObj));
        artifact.setOverriddenLicenses(mapLicenses("overriddenLicenses", licenseDataObj));
        JSONObject securityDataObj = (JSONObject) obj.get("securityData");
        artifact.setSecurityIssues(mapSecurityIssues("securityIssues", securityDataObj));
        return artifact;
    }

    private Issues mapSecurityIssues(String identifier, JSONObject securityDataObj) {
        Issues issues = new Issues();
        if ( securityDataObj != null ) {
            JSONArray securityIssueObjs = (JSONArray) securityDataObj.get(identifier);
            if (securityIssueObjs != null) {
                for (Iterator<?> it = securityIssueObjs.iterator(); it.hasNext(); ) {
                    JSONObject json = (JSONObject) it.next();
                    Issue issue = new Issue();
                    issue.setReference((String) json.get("reference"));
                    issue.setSeverity( parseSeverity( json ) );
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
            return ((Double) o).doubleValue();
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

    private ArtifactIdentifier mapIdentifier(JSONObject obj) {
        ArtifactIdentifier id = new ArtifactIdentifier();
        id.setHash((String) obj.get("hash"));
        id.setMavenCoordinates(mapMavenCoordinates((JSONObject) obj.get("componentIdentifier")));
        return id;
    }

    private MavenCoordinates mapMavenCoordinates(JSONObject object) {
        MavenCoordinates c = new MavenCoordinates();
        if (null != object) {
            JSONObject objCoordinates = (JSONObject) object.get("coordinates");
            if (null != objCoordinates) {
                c.setGroupId((String) objCoordinates.get("groupId"));
                c.setArtifactId((String) objCoordinates.get("artifactId"));
                c.setVersion((String) objCoordinates.get("version"));
            }
        }
        return c;
    }
}
