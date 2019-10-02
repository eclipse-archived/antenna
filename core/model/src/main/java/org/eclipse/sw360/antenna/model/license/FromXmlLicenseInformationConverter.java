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

import org.eclipse.sw360.antenna.model.xml.generated.LicenseClassification;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;

public class FromXmlLicenseInformationConverter {
    private FromXmlLicenseInformationConverter() {
        // only static
    }

    public static License.LicenseClassification convertLicenseClassification(LicenseClassification xmlLicenseClassification) {
        if (LicenseClassification.COVERED.equals(xmlLicenseClassification)) {
            return License.LicenseClassification.COVERED;
        } else if (LicenseClassification.NOT_CLASSIFIED.equals(xmlLicenseClassification)) {
            return License.LicenseClassification.NOT_CLASSIFIED;
        } else if (LicenseClassification.NOT_COVERED.equals(xmlLicenseClassification)) {
            return License.LicenseClassification.NOT_COVERED;
        }
        return null;
    }

    public static License.LicenseThreatGroup convertLicenseThreatGroup(LicenseThreatGroup xmlLicenseThreatGroup) {
        if (LicenseThreatGroup.FREEWARE.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.FREEWARE;
        } else if (LicenseThreatGroup.HIGH_RISK.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.HIGH_RISK;
        } else if (LicenseThreatGroup.LIBERAL.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.LIBERAL;
        } else if (LicenseThreatGroup.NON_STANDARD.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.NON_STANDARD;
        } else if (LicenseThreatGroup.NON_VERBATIM.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.NON_VERBATIM;
        } else if (LicenseThreatGroup.STRICT_COPYLEFT.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.STRICT_COPYLEFT;
        } else if (LicenseThreatGroup.UNKNOWN.equals(xmlLicenseThreatGroup)) {
            return License.LicenseThreatGroup.UNKNOWN;
        }
        return null;
    }

    public static LicenseInformation convert(org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation xmlLicenseInformation) {
        if (xmlLicenseInformation instanceof org.eclipse.sw360.antenna.model.xml.generated.License) {
            org.eclipse.sw360.antenna.model.xml.generated.License xmlLicense = (org.eclipse.sw360.antenna.model.xml.generated.License) xmlLicenseInformation;
            return new License(
                    xmlLicense.getName(),
                    xmlLicense.getLongName(),
                    xmlLicense.getText(),
                    convertLicenseClassification(xmlLicense.getClassification()),
                    convertLicenseThreatGroup(xmlLicense.getThreatGroup()));
        } else if (xmlLicenseInformation instanceof org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement) {
            org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement xmlLicenseStatement = (org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement) xmlLicenseInformation;
            final org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation xmlLeftStatement = xmlLicenseStatement.getLeftStatement();
            final org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation xmlRightStatement = xmlLicenseStatement.getRightStatement();
            final LicenseInformation leftStatement = FromXmlLicenseInformationConverter.convert(xmlLeftStatement);
            final LicenseInformation rightStatement = FromXmlLicenseInformationConverter.convert(xmlRightStatement);
            final LicenseOperator op = xmlLicenseStatement.getOp();
            if (LicenseOperator.AND.equals(op)) {
                return leftStatement.and(rightStatement);
            } else if (LicenseOperator.OR.equals(op)) {
                return leftStatement.or(rightStatement);
            } else if (leftStatement == null || leftStatement.isEmpty()) {
                    return rightStatement;
            } else if (rightStatement == null || rightStatement.isEmpty()) {
                return leftStatement;
            } else {
                // both statements are not null, but operator could not be parsed. Fall back to AND
                return leftStatement.and(rightStatement);
            }
        }
        return null;
    }
}
