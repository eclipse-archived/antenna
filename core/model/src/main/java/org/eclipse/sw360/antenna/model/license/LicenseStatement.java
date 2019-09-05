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

import com.here.ort.spdx.SpdxExpression;
import com.here.ort.spdx.SpdxLicenseIdExpression;

import java.util.HashMap;
import java.util.Map;

public class LicenseStatement extends LicenseInformation {
    private Map<String,License> licenses = new HashMap<>();
    private SpdxExpression expression;

    public LicenseStatement(License license) {
        licenses.put(license.getShortname(), license);
        expression = new SpdxLicenseIdExpression(license.getShortname(), false);
    }

    public LicenseStatement(String expression) {
        this(SpdxExpression.parse(expression));
    }

    public LicenseStatement(SpdxExpression expression) {
        this.expression = expression;
        expression.licenses()
                .forEach(sn -> {
                    final License.Builder builder = new License.Builder();
                    builder.setShortname(sn);
                    final License license = builder.build();
                    licenses.put(sn, license);
                });
    }

    @Override
    public String toSpdxExpression() {
        return expression.toString();
    }

    @Override
    public LicenseInformation and(LicenseInformation other) {
        if (other instanceof License) {
            return and((License) other);
        }
        if (other instanceof LicenseStatement) {
            return and((LicenseStatement) other);
        }
        if (other instanceof EmptyLicenseInformation) {
            return this;
        }
        throw new UnsupportedOperationException("The type of other=[" + other.getClass().getCanonicalName() + "] is unsupported");
    }

    private LicenseStatement and(License license) {
        licenses.put(license.getShortname(), license);
        expression = expression.and(new SpdxLicenseIdExpression(license.getShortname(), false));
        return this;
    }

    private LicenseStatement and(LicenseStatement licenseStatement) {
        licenses.putAll(licenseStatement.licenses);
        expression = expression.and(licenseStatement.expression);
        return this;
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        if (other instanceof License) {
            return or((License) other);
        }
        if (other instanceof LicenseStatement) {
            return or((LicenseStatement) other);
        }
        if (other instanceof EmptyLicenseInformation) {
            return this;
        }
        throw  new UnsupportedOperationException("The type of other=[" + other.getClass().getCanonicalName() + "] is unsupported");
    }

    private LicenseStatement or(License license) {
        licenses.put(license.getShortname(), license);
        expression = expression.or(new SpdxLicenseIdExpression(license.getShortname(), false));
        return this;
    }

    private LicenseStatement or(LicenseStatement licenseStatement) {
        licenses.putAll(licenseStatement.licenses);
        expression = expression.or(licenseStatement.expression);
        return this;
    }
}
