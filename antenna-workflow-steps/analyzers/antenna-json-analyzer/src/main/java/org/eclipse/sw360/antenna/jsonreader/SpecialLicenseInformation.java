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

package org.eclipse.sw360.antenna.jsonreader;

import org.eclipse.sw360.antenna.model.artifact.facts.MissingLicenseReasons;

import java.util.HashMap;
import java.util.Map;

public class SpecialLicenseInformation {
    public static final Map<String, MissingLicenseReasons> SPECIAL_INFORMATION
            = new HashMap<String, MissingLicenseReasons>()
    {{
        put("No-Sources", MissingLicenseReasons.NO_SOURCES);
        put("No-Source-License", MissingLicenseReasons.NO_LICENSE_IN_SOURCES);
        put("Not-Declared", MissingLicenseReasons.NOT_DECLARED);
        put("Not-Provided", MissingLicenseReasons.NOT_PROVIDED);
        put("Not-Supported", MissingLicenseReasons.NOT_SUPPORTED);
        put("Non-Standard", MissingLicenseReasons.NON_STANDARD);
    }};
}
