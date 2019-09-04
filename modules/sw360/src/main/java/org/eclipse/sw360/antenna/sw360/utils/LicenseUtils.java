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
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LicenseUtils {

    public static License makeLicenseFromLicenseDetails(SW360License licenseDetails) {
        License license = new License();
        license.setName(licenseDetails.getShortName());
        license.setLongName(licenseDetails.getFullName());
        license.setText(licenseDetails.getText());
        return license;
    }

    /*
     * This function takes a SPDX-like license expression as String and tries to parse it, with the following limitations
     * - it only supports expressions without the `WITH` operator
     * - it only supports expressions which either only contain AND or OR, not mixed
     */
    public static LicenseInformation makeLicenseStatementFromString(String licenseExpression) {
        if (licenseExpression == null) {
            return null;
        }

        if (licenseExpression.contains(" WITH ") || licenseExpression.contains(" with ")) {
            throw new IllegalArgumentException("The License expression=[" + licenseExpression + "] contains the unsupported WITH feature");
        }

        boolean containsOr = licenseExpression.contains(" OR ") || licenseExpression.contains(" or ");
        boolean containsAnd = licenseExpression.contains(" AND ") || licenseExpression.contains(" and ");
        LicenseOperator operator = null;
        final Stream<String> stream;
        if(containsOr && containsAnd) {
            throw new IllegalArgumentException("The License expression=[" + licenseExpression + "] contains both AND and OR which is not yet supported");
        } else if (containsAnd) {
            stream = Arrays.stream(licenseExpression.split(" AND | and "));
            operator = LicenseOperator.AND;
        } else if (containsOr) {
            stream = Arrays.stream(licenseExpression.split(" OR | or "));
            operator = LicenseOperator.OR;
        } else {
            stream = Stream.of(licenseExpression);
        }

        return makeLicenseStatementFromList(
                stream.map(n -> n.replaceAll("^[( ]+", ""))
                        .map(n -> n.replaceAll("[ )]+$", ""))
                        .filter(n -> ! n.isEmpty())
                        .map(ln -> {
                            License license = new License();
                            license.setName(ln);
                            return license;
                        }).collect(Collectors.toList()), operator);
    }

    public static LicenseInformation makeLicenseStatementFromList(List<License> licenses) {
        return makeLicenseStatementFromList(licenses, LicenseOperator.AND);
    }

    public static LicenseInformation makeLicenseStatementFromList(List<License> licenses, LicenseOperator operator) {
        if (licenses.size() == 0) {
            return new LicenseStatement();
        } else if (licenses.size() == 1) {
            return licenses.get(0);
        }

        BinaryOperator<LicenseInformation> combiner = (LicenseInformation l1, LicenseInformation l2) -> {
            LicenseStatement newLicenseStatement = new LicenseStatement();
            newLicenseStatement.setLeftStatement(l1);
            newLicenseStatement.setOp(operator);
            newLicenseStatement.setRightStatement(l2);
            return newLicenseStatement;
        };

        LicenseInformation init = licenses.get(0);
        licenses.remove(0);
        return licenses.stream()
                .reduce(init, combiner, combiner);
    }
}
