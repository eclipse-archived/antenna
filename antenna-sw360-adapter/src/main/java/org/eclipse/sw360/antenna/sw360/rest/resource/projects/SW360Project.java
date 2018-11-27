/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest.resource.projects;

import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;

import java.util.HashMap;
import java.util.Map;

public class SW360Project extends SW360HalResource<LinkObjects, SW360ProjectEmbedded> {
    private String type;
    private String name;
    private String version;
    private SW360ProjectType projectType;
    private String description;
    private Map<String, String> externalIds;
    private String createdOn;
    private String businessUnit;
    private String clearingTeam;
    private SW360Visibility visibility;
    private Map<String, SW360ProjectReleaseRelationship> releaseIdToUsage;

    public SW360Project() {}

    public SW360Project(IProject project, SW360User user) {
        name = project.getProjectId();
        version = project.getVersion();
        businessUnit = user.getDepartment();
        description = name + " " + version;
        projectType = SW360ProjectType.CUSTOMER;
        visibility = SW360Visibility.BUISNESSUNIT_AND_MODERATORS;
    }

    public String getType() {
        return this.type;
    }

    public SW360Project setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public SW360Project setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public SW360Project setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public SW360Project setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, String> getExternalIds() {
        return this.externalIds;
    }

    public SW360Project setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
        return this;
    }

    public String getCreatedOn() {
        return this.createdOn;
    }

    public SW360Project setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getBusinessUnit() {
        return this.businessUnit;
    }

    public SW360Project setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
        return this;
    }

    public SW360ProjectType getProjectType() {
        return this.projectType;
    }

    public SW360Project setProjectType(SW360ProjectType projectType) {
        this.projectType = projectType;
        return this;
    }

    public String getClearingTeam() {
        return this.clearingTeam;
    }

    public SW360Project setClearingTeam(String clearingTeam) {
        this.clearingTeam = clearingTeam;
        return this;
    }

    public SW360Visibility getVisibility() {
        return this.visibility;
    }

    public SW360Project setVisibility(SW360Visibility visbility) {
        this.visibility = visbility;
        return this;
    }

    public Map<String, SW360ProjectReleaseRelationship> getReleaseIdToUsage() {
        if (this.releaseIdToUsage == null) {
            this.releaseIdToUsage = new HashMap<>();
            // TODO: Uncomment after adding feature with linked releases in projects
            //if ((this.get_Embedded() != null) && (this.get_Embedded().getContainedReleases() != null)) {
            //this.releaseIdToUsage = this.get_Embedded().getContainedReleases()
            //			.stream()
            //			.collect(Collectors.toMap(r -> r.getReleaseId(),
            //					r -> new SW360ProjectReleaseRelationship(SW360ReleaseRelationship.CONTAINED, SW360MainlineState.MAINLINE)));
            //}
        }
        return this.releaseIdToUsage;
    }

    public SW360Project setReleaseIdToUsage(Map<String, SW360ProjectReleaseRelationship> releaseIdToUsage) {
        this.releaseIdToUsage = releaseIdToUsage;
        return this;
    }
}
