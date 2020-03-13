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

package org.eclipse.sw360.antenna.knowledgebase.spdx;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.*;

import java.nio.charset.Charset;
import java.util.Optional;

public class SPDXLicenseKnowledgeBase implements ILicenseManagementKnowledgeBase {
    private IProcessingReporter reporter;
    private ListedLicenses listedLicenses;

    public SPDXLicenseKnowledgeBase() {
        listedLicenses = ListedLicenses.getListedLicenses();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isRunnable() {
        return listedLicenses.getSpdxListedLicenseIds().length != 0;
    }

    private Optional<SpdxListedLicense> getSpdxLicense(String licenseId) {
        final SpdxListedLicense listedLicenseById;
        try {
            listedLicenseById = listedLicenses.getListedLicenseById(licenseId);
        } catch (InvalidSPDXAnalysisException e) {
            reporter.add(licenseId, MessageType.UNKNOWN_LICENSE, "No license found in SPDX with list version: " + LicenseInfoFactory.getLicenseListVersion());
            return Optional.empty();
        }
        return Optional.of(listedLicenseById);
    }

    @Override
    public void init(IProcessingReporter reporter, Charset encoding){
        this.reporter = reporter;
    }

    @Override
    public String getLicenseNameForId(String licenseId) {
        return getSpdxLicense(licenseId)
                .map(SimpleLicensingInfo::getName)
                .orElseGet(() -> {
                    reporter.add(licenseId, MessageType.MISSING_LICENSE_INFORMATION, "No license name in SPDX with list version: " + LicenseInfoFactory.getLicenseListVersion() + ", fall back to id");
                    return licenseId;
                });
    }

    @Override
    public String getTextForId(String licenseId) {
        return getSpdxLicense(licenseId)
                .map(License::getLicenseText)
                .orElseGet(() -> {
                    reporter.add(licenseId, MessageType.MISSING_LICENSE_TEXT, "No license text found in SPDX with list version: " + LicenseInfoFactory.getLicenseListVersion());
                    return null;
                });
    }

    @Override
    public String getLicenseIdForAlias(String id) {
        return getSpdxLicense(id)
                .map(SimpleLicensingInfo::getLicenseId)
                .orElseGet(() -> {
                    reporter.add(id, MessageType.MISSING_LICENSE_INFORMATION, "No license alias in SPDX with list version: " + LicenseInfoFactory.getLicenseListVersion() + ", fall back to id");
                    return id;
                });
    }

    @Override
    public String getClassificationById(String id) {
        return "";
    }

    @Override
    public String getThreatGroupForId(String id) {
        return "Unknown";
    }
}
