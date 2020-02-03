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
package org.eclipse.sw360.antenna.model.license;

import java.util.*;

public class License implements LicenseInformation {
    private static final String THREAT_GROUP_KEY = "threatGroup";
    private static final String CLASSIFICATION_KEY = "classification";

    private String name;
    private String longName;
    private String text;
    private Map<String, String> properties;

    public License() {
        properties = new HashMap<>();
    }

    public License(String name) {
        this(name, "", "", new HashMap<>());
    }

    public License(String name, String longName, String text) {
        this(name, longName, text, new HashMap<>());
    }

    public License(String name, String longName, String text, Map<String, String> properties) {
        this.name = name;
        this.longName = longName;
        this.text = text;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            return new HashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, String property) {
        properties.put(key, property);
    }

    public void setThreatGroup(String value) {
        setProperty(THREAT_GROUP_KEY, value);
    }

    public Optional<String> getThreatGroup() {
        return getProperty(THREAT_GROUP_KEY);
    }

    public void setClassification(String value) {
        setProperty(CLASSIFICATION_KEY, value);
    }

    public Optional<String> getClassification() {
        return getProperty(CLASSIFICATION_KEY);
    }

    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    @Override
    public String evaluate() {
        return getName();
    }

    @Override
    public String evaluateLong() {
        return getLongName();
    }

    @Override
    public boolean isEmpty() {
        return this.name == null;
    }

    @Override
    public List<License> getLicenses() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(name, license.name) &&
                Objects.equals(longName, license.longName) &&
                Objects.equals(text, license.text) &&
                Objects.equals(properties, license.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, longName, text, properties);
    }

    @Override
    public String toString() {
        return evaluate();
    }
}
