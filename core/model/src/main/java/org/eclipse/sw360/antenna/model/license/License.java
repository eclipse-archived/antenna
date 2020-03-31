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

/**
 * This class reflects a single Open Source License. A license comprises of a id, which is a SPDX identifier in the
 * normal case or something that could become an identifier for unknown licenses, a name which is a more user friendly
 * name of the license, e.g., for the id 'Apache-2.0', the name could be 'Apache License Version 2.0'. The class also
 * stores the text and some internal properties that can be used by an organization to process licenses in their
 * Open Source Management Process. Two properties have been predefined, a 'threat group', that can be used, e.g.,
 * to store information whether the license is permissive or has a copyleft effect. The 'classification' can be used
 * to mark a license for special handling, e.g., marking a 'BSD-4-Clause' to need special treatment in fulfilling
 * the obligations. The class is mutable, so constructors are reflected by additional setters to change a license during
 * the usage of the license in a workflow.
 */
public class License implements LicenseInformation {
    private static final String THREAT_GROUP_KEY = "threatGroup";
    private static final String CLASSIFICATION_KEY = "classification";

    private String id;
    private String commonName;
    private String text;
    private Map<String, String> properties;

    /**
     * Constructor to create an empty license object.
     */
    public License() {
        this(null, null, null, null);
    }

    /**
     * Constructor to create a license with only a license id. This is enough to identify a license, within the
     * workflow. Corresponding to this, the {@link #equalLicense(License)} method checks for equality of licenses
     * only based on the id.
     *
     * @param id The id of the license, normally a SPDX identifier, must not be null.
     */
    public License(String id) {
        this(id, null, null, null);
    }

    /**
     * Constructor for a full license, including id, name and license text.
     *
     * @param id The id of the license, normally a SPDX identifier, must not be null.
     * @param commonName The name of the license in a more verbal form, must not be null.
     * @param text The license text of the license as a formatted text string, must not be null.
     */
    public License(String id, String commonName, String text) {
        this(id, commonName, text, null);
    }

    /**
     * Constructor for a full license including in addition to id, name and license text also a map of properties,
     * like the threat group or the classification.
     *
     * @param id The id of the license, normally a SPDX identifier, must not be null.
     * @param commonName The name of the license in a more verbal form, must not be null.
     * @param text The license text of the license as a formatted text string, must not be null.
     * @param properties A map with properties of the license, must not be null.
     */
    public License(String id, String commonName, String text, Map<String, String> properties) {
        this.id = id != null ? id : "";
        this.commonName = commonName != null ? commonName : "";
        this.text = text != null ? text : "";
        this.properties = new HashMap<>();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * @return The id of the license, normally a SPDX identifier, never null, but could be empty.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id of the license, normally a SPDX identifier, must not be null.
     */
    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    /**
     * @return The name of the license, a more verbal form of the id, never null, but could be empty.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * @param commonName The name of the license in a more verbal form, must not be null.
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName != null ? commonName : "";
    }

    /**
     * @return The license text of the license as a preformatted text string, never null, but could be empty.
     */
    public String getText() {
        return text;
    }

    /**
     * @param text The license text of the license as a formatted text string, must not be null.
     */
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    /**
     * @return The unmodifiable map of properties of the license, never null, but could be empty.
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * @param properties Replace the current properties by the new property map, if given map is null, an empty map is
     *                   created.
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = new HashMap<>();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Set a new property or replace an existing one.
     *
     * @param key The key of the property to set, must not be null.
     * @param property The new value of the property, must not be null.
     */
    public void setProperty(String key, String property) {
        if (key != null) {
            properties.put(key, property);
        }
    }

    /**
     * Return the value of a property or an empty Optional.
     *
     * @param key The key of the queried property, must not be null.
     * @return The property value stored or an empty Optional.
     */
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(key != null ? properties.get(key) : null);
    }

    /**
     * A convenience setter for the predefined property 'threat group'.
     *
     * @param value The new value of the property, must not be null.
     */
    public void setThreatGroup(String value) {
        setProperty(THREAT_GROUP_KEY, value);
    }

    /**
     * A convenience getter for the predefined property 'threat group'.
     *
     * @return The property value stored or an empty Optional.
     */
    public Optional<String> getThreatGroup() {
        return getProperty(THREAT_GROUP_KEY);
    }

    /**
     * A convenience setter for the predefined property 'classification'.
     *
     * @param value The new value of the property, must not be null.
     */
    public void setClassification(String value) {
        setProperty(CLASSIFICATION_KEY, value);
    }

    /**
     * A convenience getter for the predefined property 'classification'.
     *
     * @return The property value stored or an empty Optional.
     */
    public Optional<String> getClassification() {
        return getProperty(CLASSIFICATION_KEY);
    }

    @Override
    public String evaluate() {
        return getId();
    }

    @Override
    public boolean isEmpty() {
        return "".equals(this.id.trim());
    }

    @Override
    public Collection<License> getLicenses() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(id, license.id) &&
                Objects.equals(commonName, license.commonName) &&
                Objects.equals(text, license.text) &&
                Objects.equals(properties, license.properties);
    }

    /**
     * Compares the license to another license. The licenses are equals, if the license ids are the same,
     * independent whether one license is complete or not.
     *
     * @param license The license to compare this license to.
     * @return True, if both licenses have the same license id.
     */
    public boolean equalLicense(License license) {
        return Objects.equals(getId(), license.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commonName, text, properties);
    }

    @Override
    public String toString() {
        return evaluate();
    }
}
