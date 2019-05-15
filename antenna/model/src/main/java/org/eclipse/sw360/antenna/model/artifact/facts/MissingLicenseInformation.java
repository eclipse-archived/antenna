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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MissingLicenseInformation implements ArtifactFact<MissingLicenseInformation> {

    private final List<MissingLicenseReasons> missingLicenseReasons = new ArrayList<>();

    public MissingLicenseInformation(List<MissingLicenseReasons> missingLicenseReasons) {
        this.missingLicenseReasons.addAll(missingLicenseReasons);
    }

    public List<MissingLicenseReasons> getMissingLicenseReasons() {
        return missingLicenseReasons;
    }

    @Override
    public String getFactContentName() {
        return "Reasons for missing license information";
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String prettyPrint() {
        return missingLicenseReasons.stream().map(reason -> reason.prettyPrintReason).collect(Collectors.joining(" AND "));
    }
}
