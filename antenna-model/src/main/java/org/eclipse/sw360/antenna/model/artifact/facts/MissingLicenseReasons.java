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


public enum MissingLicenseReasons {
    NOT_DECLARED("No license was declared by the project or a declared license could not be found."),
    NO_SOURCES("No sources provided hence licenses cannot be found."),
    NO_LICENSE_IN_SOURCES("Sources are provided but do not contain license headers."),
    NOT_PROVIDED("No license was specified."),
    NOT_SUPPORTED("Retrieving license information failed for this component"),
    NON_STANDARD("Configured license is of non-standard threat-group");

    public final String prettyPrintReason;

    MissingLicenseReasons(String prettyPrintReason) {
        this.prettyPrintReason = prettyPrintReason;
    }
}
