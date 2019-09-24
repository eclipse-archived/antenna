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
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;

import java.util.HashMap;
import java.util.Map;

public class LicenseStatement extends LicenseInformation {
    private final Map<String,License> licenses = new HashMap<>();
    private final SpdxExpression expression;

    public LicenseStatement(LicenseInformation licenseInformation) {
        if (licenseInformation instanceof LicenseStatement) {
            LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
            licenses.putAll(licenseStatement.licenses);
            expression = deepCopySpdxExpression(licenseStatement.expression);
        } else if (licenseInformation instanceof License) {
            License license = (License) licenseInformation;
            addLicenseToLicenses(license);
            expression = new SpdxLicenseIdExpression(license.getShortname(), false);
        } else if (licenseInformation instanceof EmptyLicenseInformation) {
            // no-op
            expression = SpdxExpression.parse("");
        } else {
            throw new AntennaExecutionException("Unsupported type of LicenseInformation");
        }
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

    private void addLicenseToLicenses(License license) {
        licenses.put(license.getShortname(), license);
    }

    private SpdxExpression deepCopySpdxExpression(SpdxExpression spdxExpression) {
        // TODO: how to make a deep copy?
        return new SpdxLicenseIdExpression(spdxExpression.toString(), false);
    }

    @Override
    public String toSpdxExpression() {
        return expression.toString();
    }

    @Override
    public boolean isEmpty() {
        return licenses.size() == 0;
    }

    @Override
    public LicenseInformation and(LicenseInformation other) {
        if (other instanceof License) {
            License license = (License) other;
            LicenseStatement clone = new LicenseStatement(this);
            clone.addLicenseToLicenses(license);
            clone.expression.and(new SpdxLicenseIdExpression(license.getShortname(), false));
            return this;
        }
        if (other instanceof LicenseStatement) {
            LicenseStatement licenseStatement = (LicenseStatement) other;
            LicenseStatement clone = new LicenseStatement(this);
            clone.licenses.putAll(licenseStatement.licenses);
            clone.expression.and(deepCopySpdxExpression(licenseStatement.expression));
            return clone;
        }
        if (other instanceof EmptyLicenseInformation) {
            return new LicenseStatement(this);
        }
        throw new UnsupportedOperationException("The type of other=[" + other.getClass().getCanonicalName() + "] is unsupported");
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        if (other instanceof License) {
            License license = (License) other;
            LicenseStatement clone = new LicenseStatement(this);
            clone.addLicenseToLicenses(license);
            clone.expression.or(new SpdxLicenseIdExpression(license.getShortname(), false));
            return this;
        }
        if (other instanceof LicenseStatement) {
            LicenseStatement licenseStatement = (LicenseStatement) other;
            LicenseStatement clone = new LicenseStatement(this);
            clone.licenses.putAll(licenseStatement.licenses);
            clone.expression.or(deepCopySpdxExpression(licenseStatement.expression));
            return clone;
        }
        if (other instanceof EmptyLicenseInformation) {
            return new LicenseStatement(this);
        }
        throw  new UnsupportedOperationException("The type of other=[" + other.getClass().getCanonicalName() + "] is unsupported");
    }
}
