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
    private final String shortname;
    private final String fullname;
    private final String text;
    private final LicenseClassification licenseClassification;
    private final LicenseThreatGroup licenseThreatGroup;

    public License(String shortname, String fullname, String text, LicenseClassification licenseClassification, LicenseThreatGroup licenseThreatGroup) {
        this.shortname = shortname;
        this.fullname = fullname;
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

    public String getShortname() {
        return shortname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toSpdxExpression() {
        return getShortname();
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
        return Objects.equals(shortname, license.shortname) &&
                Objects.equals(fullname, license.fullname) &&
                Objects.equals(text, license.text) &&
                licenseClassification == license.licenseClassification &&
                licenseThreatGroup == license.licenseThreatGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortname, fullname, text, licenseClassification, licenseThreatGroup);
    }

    public enum LicenseClassification {
        // TODO
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
        private String shortname;
        private String fullname;
        private String text;
        private License.LicenseClassification licenseClassification;
        private License.LicenseThreatGroup licenseThreatGroup = LicenseThreatGroup.UNKNOWN;

        public Builder setShortname(String shortname) {
            this.shortname = shortname;
            return this;
        }

        public Builder setFullname(String fullname) {
            this.fullname = fullname;
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
            return new License(shortname, fullname, text, licenseClassification, licenseThreatGroup);
        }
    }
}
