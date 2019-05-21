/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.testing;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseClassification;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;

import java.nio.charset.Charset;

public class TestLicenseKnowledgeBase implements ILicenseManagementKnowledgeBase {

    @Override
    public void init(IProcessingReporter reporter, Charset encoding){
    }

    @Override
    public String getLicenseNameForId(String licenseId) {
        return "LicenseNameFor:"+licenseId;
    }

    @Override
    public String getTextForId(String licenseId) {
        return "LicenseTextFor:"+licenseId;
    }

    @Override
    public String getLicenseIdForAlias(String id) {
        return id;
    }

    @Override
    public LicenseClassification getClassificationById(String id) {
        return LicenseClassification.NOT_CLASSIFIED;
    }

    @Override
    public LicenseThreatGroup getThreatGroupForId(String id) {
        return LicenseThreatGroup.UNKNOWN;
    }
}
