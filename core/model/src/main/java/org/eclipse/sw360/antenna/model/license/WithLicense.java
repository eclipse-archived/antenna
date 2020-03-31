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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithLicense implements LicenseInformation {
    private License license;
    private License exception;

    public WithLicense() {
        this(new License(), new License());
    }

    public WithLicense(License license, License exception) {
        this.license = license;
        this.exception = exception;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public License getException() {
        return exception;
    }

    public void setException(License exception) {
        this.exception = exception;
    }

    @Override
    public String evaluate() {
        return license.getId() + " WITH " + exception.getId();
    }

    @Override
    public boolean isEmpty() {
        return license.isEmpty() && exception.isEmpty();
    }

    @Override
    public List<License> getLicenses() {
        return Stream.of(license, exception).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WithLicense that = (WithLicense) o;
        return Objects.equals(license, that.license) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(license, exception);
    }

    @Override
    public String toString() {
        return evaluate();
    }
}
