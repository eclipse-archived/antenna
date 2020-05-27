/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import org.ossreviewtoolkit.spdx.*;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.license.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses) {
        return mapLicenses(licenses, LicenseOperator.AND);
    }

    public static LicenseInformation mapLicenses(Collection<String> licenses, LicenseOperator operator) {
        Iterator<String> iterator = licenses.iterator();
        if (!iterator.hasNext()) {
            return new License();
        }

        License license = new License();
        license.setId(iterator.next());
        if (licenses.size() == 1) {
            return license;
        } else {
            LicenseStatement licenseStatement = new LicenseStatement();
            licenseStatement.setOp(operator);
            licenseStatement.setLicenses(
                    licenses
                            .stream()
                            .map(License::new)
                            .collect(Collectors.toList())
            );
            return licenseStatement;
        }
    }

    public static LicenseInformation fromSPDXExpression(String spdxExpressionString) {
        SpdxExpression spdxExpression = SpdxExpression.parse(spdxExpressionString);
        return fromSPDXExpression(spdxExpression);
    }

    public static LicenseInformation fromSPDXExpression(SpdxExpression spdxExpression) {
        if (spdxExpression instanceof SpdxCompoundExpression) {
            SpdxCompoundExpression spdxCompoundExpression = (SpdxCompoundExpression) spdxExpression;
            return fromSPDXCompoundExpression(spdxCompoundExpression, new LicenseStatement());
        }
        if (spdxExpression instanceof SpdxLicenseWithExceptionExpression) {
            SpdxLicenseWithExceptionExpression spdxWithExceptionExpression = (SpdxLicenseWithExceptionExpression) spdxExpression;
            return fromSpdxWithLicense(spdxWithExceptionExpression);
        }
        if (spdxExpression instanceof SpdxLicenseIdExpression) {
            SpdxLicenseIdExpression spdxLicenseIdExpression = (SpdxLicenseIdExpression) spdxExpression;
            return fromSpdxLicenseIdExpression(spdxLicenseIdExpression);
        }
        if (spdxExpression instanceof SpdxLicenseReferenceExpression) {
            SpdxLicenseReferenceExpression spdxLicenseReferenceExpression = (SpdxLicenseReferenceExpression) spdxExpression;
            return fromSpdxLicenseReferenceExpression(spdxLicenseReferenceExpression);
        }
        throw new ExecutionException("SPDX expression=[" + spdxExpression.toString() + "] could not be parsed");
    }

    public static LicenseInformation parseSpdxExpression(String expression) {
        try {
            return LicenseSupport.fromSPDXExpression(expression);
        } catch (SpdxException e) {
            License unparsableExpression = new License();
            unparsableExpression.setId(expression);
            return unparsableExpression;
        }
    }

    private static LicenseInformation fromSpdxWithLicense(SpdxLicenseWithExceptionExpression spdxWithExceptionExpression) {
        final SpdxSimpleExpression license = spdxWithExceptionExpression.getLicense();
        final String exception = spdxWithExceptionExpression.getException();
        final License withLicense = (License) fromSPDXExpression(license);
        final License exceptionLicense = (License) fromSPDXExpression(exception);
        return new WithLicense(withLicense.getId(), withLicense.getCommonName(), withLicense.getText(),
                exceptionLicense.getId(), exceptionLicense.getCommonName(), exceptionLicense.getText());
    }

    public static LicenseInformation fromSpdxLicenseReferenceExpression(SpdxLicenseReferenceExpression spdxLicenseReferenceExpression) {
        String licenseId = spdxLicenseReferenceExpression.component1();
        License license = new License();
        license.setId(licenseId);
        return license;
    }

    public static LicenseInformation fromSpdxLicenseIdExpression(SpdxLicenseIdExpression spdxLicenseIdExpression) {
        String licenseId = spdxLicenseIdExpression.component1();
        boolean isOrLater = spdxLicenseIdExpression.component2();
        if (isOrLater) {
            licenseId += "+";
        }
        License license = new License();
        license.setId(licenseId);
        return license;
    }

    public static LicenseInformation fromSPDXCompoundExpression(SpdxCompoundExpression spdxCompoundExpression, LicenseStatement license) {
        final SpdxExpression left = spdxCompoundExpression.component1();
        final SpdxOperator operator = spdxCompoundExpression.component2();
        final SpdxExpression right = spdxCompoundExpression.component3();

        if (SpdxOperator.AND.equals(operator)) {
            license.setOp(LicenseOperator.AND);
        }
        if (SpdxOperator.OR.equals(operator)) {
            license.setOp(LicenseOperator.OR);
        }
        if (left instanceof SpdxCompoundExpression) {
            checkCompoundChild((SpdxCompoundExpression) left, license);
        } else {
            license.addLicenseInformation(fromSPDXExpression(left));
        }
        if (right instanceof SpdxCompoundExpression) {
            checkCompoundChild((SpdxCompoundExpression) right, license);
        } else {
            license.addLicenseInformation(fromSPDXExpression(right));
        }

        return license;
    }

    private static LicenseInformation checkCompoundChild(SpdxCompoundExpression spdxCompoundExpression, LicenseStatement license) {
        final SpdxExpression left = spdxCompoundExpression.component1();
        final SpdxOperator operator = spdxCompoundExpression.component2();
        final SpdxExpression right = spdxCompoundExpression.component3();
        if (operator.toString().equals(license.getOp().toString())) {
            if (left instanceof SpdxCompoundExpression) {
                checkCompoundChild((SpdxCompoundExpression) left, license);
            } else {
                license.addLicenseInformation(fromSPDXExpression(left));
            }
            if (right instanceof SpdxCompoundExpression) {
                checkCompoundChild((SpdxCompoundExpression) right, license);
            } else {
                license.addLicenseInformation(fromSPDXExpression(right));
            }
        } else {
            license.addLicenseInformation(fromSPDXExpression(spdxCompoundExpression));
        }
        return license;
    }
}
