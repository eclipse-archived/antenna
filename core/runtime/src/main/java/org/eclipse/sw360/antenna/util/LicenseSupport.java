/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import com.here.ort.spdx.*;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

import java.util.Collection;
import java.util.Iterator;

public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses) {
        return mapLicenses(licenses, LicenseOperator.AND);
    }

    public static LicenseInformation mapLicenses(Collection<String> licenses, LicenseOperator operator) {
        Iterator<String> iterator = licenses.iterator();
        if (!iterator.hasNext()) {
            return new LicenseStatement();
        }

        return computeRecursiveLicenseStatement(iterator, new LicenseStatement(), operator);
    }

    private static LicenseInformation computeRecursiveLicenseStatement(
            Iterator<String> iterator, LicenseStatement leftLicense, LicenseOperator operator) {
        License license = new License();
        license.setName(iterator.next());

        if (iterator.hasNext()) {
            leftLicense.setLeftStatement(license);
            leftLicense.setOp(operator);
            leftLicense.setRightStatement(computeRecursiveLicenseStatement(iterator, new LicenseStatement(), operator));
            return leftLicense;
        }
        return license;
    }

    public static LicenseInformation fromSPDXExpression(String spdxExpressionString) {
        SpdxExpression spdxExpression = SpdxExpression.parse(spdxExpressionString);
        return fromSPDXExpression(spdxExpression);
    }

    public static LicenseInformation fromSPDXExpression(SpdxExpression spdxExpression) {
        if (spdxExpression instanceof SpdxCompoundExpression) {
            SpdxCompoundExpression spdxCompoundExpression = (SpdxCompoundExpression) spdxExpression;
            final SpdxExpression left = spdxCompoundExpression.component1();
            final SpdxOperator operator = spdxCompoundExpression.component2();
            final SpdxExpression right = spdxCompoundExpression.component3();

            LicenseStatement result = new LicenseStatement();
            result.setLeftStatement(fromSPDXExpression(left));
            result.setRightStatement(fromSPDXExpression(right));
            if(SpdxOperator.AND.equals(operator)){
                result.setOp(LicenseOperator.AND);
                return result;
            }
            if (SpdxOperator.OR.equals(operator)) {
                result.setOp(LicenseOperator.OR);
                return result;
            }
            if (SpdxOperator.WITH.equals(operator)) {
                throw new ExecutionException("SPDX expression=[" + spdxExpression.toString() + "] contains unsupported WITH operator");
            }
        }
        if (spdxExpression instanceof SpdxLicenseIdExpression) {
            SpdxLicenseIdExpression spdxLicenseIdExpression = (SpdxLicenseIdExpression) spdxExpression;
            String licenseId = spdxLicenseIdExpression.component1();
            boolean isOrLater = spdxLicenseIdExpression.component2();
            if (isOrLater) {
                licenseId += "+";
            }
            License license = new License();
            license.setName(licenseId);
            return license;
        }
        if (spdxExpression instanceof SpdxLicenseReferenceExpression) {
            SpdxLicenseReferenceExpression spdxLicenseReferenceExpression = (SpdxLicenseReferenceExpression) spdxExpression;
            String licenseId = spdxLicenseReferenceExpression.component1();
            License license = new License();
            license.setName(licenseId);
            return license;
        }
        if (spdxExpression instanceof SpdxLicenseExceptionExpression) {
            throw new ExecutionException("SPDX expression=[" + spdxExpression.toString() + "] contains an exception, which is currently unsupported");
        }
        throw new ExecutionException("SPDX expression=[" + spdxExpression.toString() + "] could not be parsed");
    }

    public static LicenseInformation parseSpdxExpression(String expression) {
        try {
            return LicenseSupport.fromSPDXExpression(expression);
        } catch (SpdxException e) {
            License unparsableExpression = new License();
            unparsableExpression.setName(expression);
            return unparsableExpression;
        }
    }
}
