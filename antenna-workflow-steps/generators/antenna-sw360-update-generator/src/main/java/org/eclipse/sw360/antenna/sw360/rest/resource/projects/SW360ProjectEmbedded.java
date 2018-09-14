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

import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SW360ProjectEmbedded implements Embedded {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("sw360:projects")
    private List<SW360Project> projects = new ArrayList<>();
    private SW360User createdBy;

    public List<SW360Project> getProjects() {
        return this.projects;
    }

    public SW360ProjectEmbedded setProjects(List<SW360Project> projects) {
        this.projects = projects;
        return this;
    }

    public SW360User getCreatedBy() {
        return createdBy;
    }

    public SW360ProjectEmbedded setCreatedBy(SW360User createdBy) {
        this.createdBy = createdBy;
        return this;
    }
}