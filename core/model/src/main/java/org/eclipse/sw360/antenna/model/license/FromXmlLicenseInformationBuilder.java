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

public class FromXmlLicenseInformationBuilder {

    private final org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation xmlLicenseInformation;

    public FromXmlLicenseInformationBuilder(org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation xmlLicenseInformation) {
        this.xmlLicenseInformation = xmlLicenseInformation;
    }

    public LicenseInformation build() {
        if (xmlLicenseInformation instanceof org.eclipse.sw360.antenna.model.xml.generated.License) {
            return null;
        }
        if (xmlLicenseInformation instanceof org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement) {
            return null;
        }
        return null;
    }
}
