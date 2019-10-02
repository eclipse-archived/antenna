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
package org.eclipse.sw360.antenna.model.license;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class License extends LicenseInformation {
    private final String licenseId;
    private final String name;
    private final String text;
    private final LicenseClassification licenseClassification;
    private final LicenseThreatGroup licenseThreatGroup;

    public License(String licenseId, String name, String text, LicenseClassification licenseClassification, LicenseThreatGroup licenseThreatGroup) {
        this.licenseId = licenseId;
        this.name = name;
        this.text = text;
        this.licenseClassification = licenseClassification;
        this.licenseThreatGroup = licenseThreatGroup;
    }

    public LicenseClassification getLicenseClassification() {
        return licenseClassification;
    }

    public LicenseThreatGroup getLicenseThreatGroup() {
        return licenseThreatGroup;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toSpdxExpression() {
        return getLicenseId();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Set<License> getLicenses() {
        return Collections.singleton(this);
    }

    @Override
    public LicenseInformation and(LicenseInformation other) {
        return new LicenseStatement(this).and(other);
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        return new LicenseStatement(this).or(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(licenseId, license.licenseId) &&
                Objects.equals(name, license.name) &&
                Objects.equals(text, license.text) &&
                licenseClassification == license.licenseClassification &&
                licenseThreatGroup == license.licenseThreatGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseId, name, text, licenseClassification, licenseThreatGroup);
    }

    public enum LicenseClassification {
        COVERED,
        NOT_CLASSIFIED,
        NOT_COVERED;
    }

    public enum LicenseThreatGroup {
        UNKNOWN,
        LIBERAL,
        STRICT_COPYLEFT,
        HIGH_RISK,
        FREEWARE,
        NON_STANDARD,
        NON_VERBATIM;
    }

    public static class Builder {
        private String licenseId;
        private String name;
        private String text;
        private License.LicenseClassification licenseClassification;
        private License.LicenseThreatGroup licenseThreatGroup = LicenseThreatGroup.UNKNOWN;

        public Builder setLicenseId(String licenseId) {
            this.licenseId = licenseId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setLicenseClassification(License.LicenseClassification licenseClassification) {
            this.licenseClassification = licenseClassification;
            return this;
        }

        public Builder setLicenseThreatGroup(License.LicenseThreatGroup licenseThreatGroup) {
            this.licenseThreatGroup = licenseThreatGroup;
            return this;
        }

        public License build() {
            return new License(licenseId, name, text, licenseClassification, licenseThreatGroup);
        }
    }
}
