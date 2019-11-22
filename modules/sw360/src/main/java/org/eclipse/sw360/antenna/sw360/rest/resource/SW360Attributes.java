/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest.resource;

public class SW360Attributes {
    private SW360Attributes() {}

    // Project Controller Attributes
    public static final String PROJECT_SEARCH_BY_NAME = "name";
    public static final String PROJECT_RELEASES = "releases";
    public static final String PROJECT_RELEASES_TRANSITIVE ="transitive";
    public static final String COMPONENT_SEARCH_BY_NAME = "name";


    // Attributes of Sw360Project
    public static final String PROJECT_ID = "id";
    public static final String PROJECT_TYPE = "type";
    public static final String PROJECT_NAME = "name";
    public static final String PROJECT_VERSION = "version";
    public static final String PROJECT_PROJECT_TYPE = "projectType";
    public static final String PROJECT_DESCRIPTION = "description";
    public static final String PROJECT_EXTERNAL_IDS = "externalIds";
    public static final String PROJECT_CREATED_ON= "createdOn";
    public static final String PROJECT_BUSINESS_UNIT = "businessUnit";
    public static final String PROJECT_CLEARING_TEAM= "clearingTeam";
    public static final String PROJECT_VISIBILITY = "visbility";
    public static final String PROJECT_RELEASE_ID_TO_USAGE = "releaseIdToUsage";

    // Attributes of Sw360Component
    public static final String COMPONENT_ID = "id";
    public static final String COMPONENT_COMPONENT_NAME = "name";
    public static final String COMPONENT_COMPONENT_TYPE = "componentType";
    public static final String COMPONENT_TYPE = "type";
    public static final String COMPONENT_CREATED_ON = "createdOn";
    public static final String COMPONENT_CATEGORIES = "categories";
    public static final String COMPONENT_HOMEPAGE = "homepage";

    // Attributes of Sw360Release
    public static final String RELEASE_ID = "id";
    public static final String RELEASE_COMPONENT_ID = "componentId";
    public static final String RELEASE_NAME = "name";
    public static final String RELEASE_VERSION = "version";
    public static final String RELEASE_CPE_ID = "cpeId";
    public static final String RELEASE_SOURCES = "downloadurl";
    public static final String RELEASE_MAIN_LICENSE_IDS = "mainLicenseIds";
    public static final String RELEASE_EXTERNAL_IDS = "externalIds";

    // Attributes of Sw360Attachment
    public static final String ATTACHMENT_ATTACHMENT_TYPE = "attachmentType";
    public static final String ATTACHMENT_CHECK_STATUS = "checkStatus";

    // Attributes of Sw360License
    public static final String LICENSE_TYPE = "type";
    public static final String LICENSE_TEXT = "text";
    public static final String LICENSE_SHORT_NAME = "shortName";
    public static final String LICENSE_FULL_NAME = "fullName";
    public static final String LICENSE_EXTERNAL_IDS = "externalIds";

    // Attributes of Sw360Authenticator
    public static final String AUTHENTICATOR_GRANT_TYPE = "grant_type";
    public static final String AUTHENTICATOR_USERNAME = "username";
    public static final String AUTHENTICATOR_PASSWORD = "password";
}
