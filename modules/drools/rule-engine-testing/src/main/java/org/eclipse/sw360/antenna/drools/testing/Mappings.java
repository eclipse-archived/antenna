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

package org.eclipse.sw360.antenna.drools.testing;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Mappings {
    static final Map<String, MatchState> MATCH_STATE_MAP =
            new HashMap<String, MatchState>() {{
                put("unknown", MatchState.UNKNOWN);
                put("similar", MatchState.SIMILAR);
                put("exact", MatchState.EXACT);
            }};
    static final Map<String, LicenseThreatGroup> THREAT_GROUP_MAP =
            new HashMap<String, LicenseThreatGroup>() {{
                put("unknown", LicenseThreatGroup.UNKNOWN);
                put("liberal", LicenseThreatGroup.LIBERAL);
                put("strict copyleft", LicenseThreatGroup.STRICT_COPYLEFT);
                put("high risk", LicenseThreatGroup.HIGH_RISK);
                put("freeware", LicenseThreatGroup.FREEWARE);
                put("non standard", LicenseThreatGroup.NON_STANDARD);
            }};
    static final Map<String, Function<LicenseInformation, ArtifactLicenseInformation>> LICENSE_GROUP =
            new HashMap<String, Function<LicenseInformation, ArtifactLicenseInformation>>() {{
                put("Declared", DeclaredLicenseInformation::new);
                put("Observed", ObservedLicenseInformation::new);
                put("Overridden", OverriddenLicenseInformation::new);
                put("Configured", ConfiguredLicenseInformation::new);
            }};
    static final Map<String, MissingLicenseReasons> MISSING_LICENSE_REASONS_MAP =
            new HashMap<String, MissingLicenseReasons>() {{
                put("Not-Declared", MissingLicenseReasons.NOT_DECLARED);
                put("No-Sources", MissingLicenseReasons.NO_SOURCES);
                put("No-Source-License", MissingLicenseReasons.NO_LICENSE_IN_SOURCES);
                put("Not-Provided", MissingLicenseReasons.NOT_PROVIDED);
                put("Not-Supported", MissingLicenseReasons.NOT_SUPPORTED);
                put("Non-Standard", MissingLicenseReasons.NON_STANDARD);
            }};

    static final Map<String, Function<List<String>, Coordinate>> COORDINATES_FACTORY =
            new HashMap<String, Function<List<String>, Coordinate>>() {{
                put("maven", row -> {
                    if (row.size() < 5) {
                        throw new ConfigurationException("Maven coordinates need to specify groupId, artifactId and version in that order");
                    }
                    return new Coordinate(Coordinate.Types.MAVEN, row.get(2), row.get(3), row.get(4));
                });
                put("generic", row -> {
                    if (row.size() < 4) {
                        throw new ConfigurationException("Generic coordinates need to specify name and version in that order");
                    }
                    return Coordinate.builder()
                            .withName(row.get(2))
                            .withVersion(row.get(3))
                            .build();
                });
                put("dotnet", row -> {
                    if (row.size() < 4) {
                        throw new ConfigurationException(".NET coordinates need to specify packageId and version in that order");
                    }
                    return new Coordinate(Coordinate.Types.NUGET, row.get(2), row.get(3));
                });
                put("bundle", row -> {
                    if (row.size() < 4) {
                        throw new ConfigurationException("Bundle coordinates need to specify BundleSymbolicName and BundleVersion in that order");
                    }
                    return new Coordinate(Coordinate.Types.P2, row.get(2), row.get(3));
                });
                put("javascript", row -> {
                    if (row.size() < 5) {
                        throw new ConfigurationException("JavaScript coordinates need to specify namespace, packageName and version in that order");
                    }
                    return new Coordinate(Coordinate.Types.NPM, row.get(2), row.get(3), row.get(4));
                });
            }};

    static final Map<String, SecurityIssueStatus> SECURITY_ISSUES =
            new HashMap<String, SecurityIssueStatus>() {{
                put("acknowledged", SecurityIssueStatus.ACKNOWLEDGED);
                put("confirmed", SecurityIssueStatus.CONFIRMED);
                put("not applicable", SecurityIssueStatus.NOT_APPLICABLE);
                put("open", SecurityIssueStatus.OPEN);
            }};

    static final Map<String, DataKeys> DATA_KEYS =
            new HashMap<String, DataKeys>() {{
                put("license", DataKeys.LICENSE);
                put("licenseMissing", DataKeys.LICENSE_MISSING);
                put("licenseAlternatives", DataKeys.LICENSE_ALTERNATIVES);
                put("proprietary", DataKeys.PROPRIETARY);
                put("matchstate", DataKeys.MATCH_STATE);
                put("coordinates", DataKeys.COORDINATES);
                put("securityIssue", DataKeys.SECURITY_ISSUE);
            }};

    private Mappings() {
        // only static members
    }
}
