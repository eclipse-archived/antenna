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

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

import java.util.Collection;
import java.util.Iterator;

public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses) {
        // The overall statement
        LicenseStatement licenseStatement = new LicenseStatement();
        // Indicator for each new statement
        LicenseStatement temporaryLStatement = new LicenseStatement();
                int c=0;
                for (Iterator<String> i = licenses.iterator(); i.hasNext();) {
                    String licenseName = i.next();
                    License license = new License();
                    license.setName(licenseName);
                    if(i.hasNext()){
                        ++c;
                        temporaryLStatement = LicenseSupport.createStatement(licenseStatement, temporaryLStatement, license);
                    }
                    else if (c==0) {
                        return license;
                    }
                    else if (c==1){
                        licenseStatement.setRightStatement(license);
                        return licenseStatement;
                    }
                    else {
                        temporaryLStatement.setRightStatement(license);
                        return licenseStatement;
                    }
        }
        return licenseStatement;
    }


    public static LicenseStatement createStatement(LicenseStatement licenseStatement, LicenseStatement temporaryLStatement, License license){
        if(temporaryLStatement.getLeftStatement() == null) {
            if (licenseStatement.getLeftStatement() == null) {
                licenseStatement.setLeftStatement(license);
                licenseStatement.setOp(LicenseOperator.AND);
                return temporaryLStatement;
            } else {
                LicenseStatement newLicenseStatement = new LicenseStatement();
                newLicenseStatement.setLeftStatement(license);
                newLicenseStatement.setOp(LicenseOperator.AND);
                licenseStatement.setRightStatement(newLicenseStatement);
                return newLicenseStatement;
            }
        }
        else{
            if (temporaryLStatement.getLeftStatement() == null) {
                temporaryLStatement.setLeftStatement(license);
                temporaryLStatement.setOp(LicenseOperator.AND);
                return temporaryLStatement;
            } else {
                LicenseStatement newLicenseStatement = new LicenseStatement();
                newLicenseStatement.setLeftStatement(license);
                newLicenseStatement.setOp(LicenseOperator.AND);
                temporaryLStatement.setRightStatement(newLicenseStatement);
                return newLicenseStatement;
            }
        }
    }
}
