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

package org.eclipse.sw360.antenna.rules;

import org.eclipse.sw360.antenna.api.IRulesPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class AntennaBasicRulesPackage implements IRulesPackage {
    private static final String POLICIES_FILE = "/policies/policies.xml";
    private static final String POLICIES_VERSION_FILE = "/policies/policies.properties";
    private static final String POLICIES_VERSION = "policies.version";
    private static final String NO_VERSION = "no version string specified";

    @Override
    public String getRulesPackageName() {
        return "Antenna Basic Rules";
    }

    @Override
    public String getRulesetFolder() {
        return getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    @Override
    public String getPoliciesAsString() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(POLICIES_FILE);
             InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {
            return bufferedReader.lines().map(String::trim).collect(Collectors.joining());
        }
    }

    @Override
    public String getVersion() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(POLICIES_VERSION_FILE)) {
            Properties appProperties = new Properties();
            appProperties.load(stream);
            return "antennaBasicRules" + ":" + Optional.ofNullable(appProperties.getProperty(POLICIES_VERSION)).orElse(NO_VERSION);
        }
    }
}
