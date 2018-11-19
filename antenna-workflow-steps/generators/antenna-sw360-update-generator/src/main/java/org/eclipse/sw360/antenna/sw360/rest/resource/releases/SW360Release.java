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
package org.eclipse.sw360.antenna.sw360.rest.resource.releases;

import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

import java.util.Set;

public class SW360Release<L extends LinkObjects, E extends Embedded> extends SW360HalResource<L, E> {
    private String componentId;
    private String name;
    private String version;
    private String cpeid;
    private Set<String> mainLicenseIds;

    public String getComponentId() {
        return componentId;
    }

    public SW360Release setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SW360Release setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SW360Release setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCpeid() {
        return cpeid;
    }

    public SW360Release setCpeid(String cpeid) {
        this.cpeid = cpeid;
        return this;
    }

    public Set<String> getMainLicenseIds() {
        return mainLicenseIds;
    }

    public SW360Release setMainLicenseIds(Set<String> mainLicenseIds) {
        this.mainLicenseIds = mainLicenseIds;
        return this;
    }
}
