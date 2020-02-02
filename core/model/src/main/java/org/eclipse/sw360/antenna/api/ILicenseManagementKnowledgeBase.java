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

package org.eclipse.sw360.antenna.api;

import java.nio.charset.Charset;

/**
 * An LicenseManagementKnowledgeBase delivers values for the Licenses of an
 * Artifact.
 */
public interface ILicenseManagementKnowledgeBase {

    default void init(IProcessingReporter reporter, Charset encoding) {

    }

    /**
     * 
     * @param licenseId
     * @return Returns the license name belonging to the licenseId.
     */
    String getLicenseNameForId(String licenseId);

    /**
     * 
     * @param id
     * @return Returns the matching text for the id.
     */

    String getTextForId(String id);

    /**
     * @param id
     * @return Returns the Alias for the id.
     */
    String getLicenseIdForAlias(String id);

    /**
     * @param id
     * @return Returns the Classification for the id.
     */
    String getClassificationById(String id);

    String getThreatGroupForId(String id);
}
