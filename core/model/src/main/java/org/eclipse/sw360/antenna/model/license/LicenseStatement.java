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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LicenseStatement implements LicenseInformation {
    private List<LicenseInformation> licenses;
    private LicenseOperator op;

    public LicenseStatement() {
        this(null, null);
    }

    public LicenseStatement(List<LicenseInformation> licenses, LicenseOperator op) {
        this.licenses = new ArrayList<>();
        if (licenses != null) {
            this.licenses.addAll(licenses);
        }
        this.op = op != null ? op : LicenseOperator.AND;
    }

    public void setLicenses(List<LicenseInformation> licenses) {
        this.licenses.clear();
        if (licenses != null) {
            this.licenses.addAll(licenses);
        }
    }

    public LicenseOperator getOp() {
        return op;
    }

    public void setOp(LicenseOperator operator) {
        this.op = operator != null ? operator : LicenseOperator.AND;
    }

    public boolean addLicenseInformation(LicenseInformation license) {
        if (license != null) {
            return licenses.add(license);
        }
        return false;
    }

    @Override
    public String evaluate() {
        if (isEmpty()) {
            return "";
        }
        return "( " +
                licenses
                        .stream()
                        .map(LicenseInformation::evaluate)
                        .collect(Collectors.joining(" " + this.op.toString() + " "))
                + " )";
    }

    @Override
    public boolean isEmpty() {
        return licenses.isEmpty() || licenses.stream().allMatch(LicenseInformation::isEmpty);
    }

    @Override
    public Collection<License> getLicenses() {
        return licenses
                .stream()
                .map(LicenseInformation::getLicenses)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicenseStatement that = (LicenseStatement) o;
        return Objects.equals(licenses, that.licenses) &&
                op == that.op;
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenses, op);
    }

    @Override
    public String toString() {
        return evaluate();
    }
}
