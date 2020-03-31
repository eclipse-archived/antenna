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
import java.util.stream.Collectors;

/**
 * This class reflects an operation in a SPDX license expression. It combines a set of {@link LicenseInformation}
 * objects with an operation, either 'AND' or 'OR'.
 */
public class LicenseStatement implements LicenseInformation {
    private Collection<LicenseInformation> licenses;
    private LicenseOperator op;

    /**
     * Constructor for an empty statement, can be enriched by setters below.
     */
    public LicenseStatement() {
        this(null, null);
    }

    /**
     * The standard constructor to create a license expression with the given license operands and the given operator.
     *
     * @param licenses The license operands of the expression, must not be null.
     * @param op The operator of the expression, must not be null.
     */
    public LicenseStatement(Collection<LicenseInformation> licenses, LicenseOperator op) {
        this.licenses = new ArrayList<>();
        if (licenses != null) {
            this.licenses.addAll(licenses);
        }
        this.op = op != null ? op : LicenseOperator.AND;
    }

    /**
     * Replace the license operands of the expression by the given Collection.
     *
     * @param licenses The license operands of the expression, must not be null.
     */
    public void setLicenses(Collection<LicenseInformation> licenses) {
        this.licenses.clear();
        if (licenses != null) {
            this.licenses.addAll(licenses);
        }
    }

    /**
     * @return The operator of the expression, never null
     */
    public LicenseOperator getOp() {
        return op;
    }

    /**
     * Reset the operator of the expression.
     *
     * @param operator The new operator, must not be null.
     */
    public void setOp(LicenseOperator operator) {
        this.op = operator != null ? operator : LicenseOperator.AND;
    }

    /**
     * Add a single operand to the expression.
     *
     * @param license The new operand to be added to the expression, must not be null.
     * @return The result of the add operation to the internal collection.
     */
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
        return licenses.stream()
                .map(LicenseInformation::evaluate)
                .collect(Collectors.joining(" " + this.op.toString() + " ", "( ", " )"));
    }

    @Override
    public boolean isEmpty() {
        return licenses.isEmpty() || licenses.stream().allMatch(LicenseInformation::isEmpty);
    }

    /**
     * This method returns the first level of operands of this operation. This can be either {@link License} objects
     * or embedded {@link LicenseStatement} objects. In comparison to getLicenses, this method does not dig into the
     * expression to return all licenses, but reflects the expression tree by returning only the next level.
     *
     * @return The direct operands of the license expression.
     */
    public Collection<LicenseInformation> getStatementOperands() {
        return Collections.unmodifiableCollection(licenses);
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
