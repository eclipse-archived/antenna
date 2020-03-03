/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.sw360.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

public class SW360ComponentClientAdapter {
    private SW360ComponentClient componentClient;

    public SW360ComponentClientAdapter setComponentClient(SW360ComponentClient componentClient) {
        if(this.componentClient == null) {
            this.componentClient = componentClient;
        }
        return this;
    }

    public Optional<SW360Component> getOrCreateComponent(SW360Component componentFromRelease, HttpHeaders header) {
        if(componentFromRelease.getComponentId() != null) {
            return getComponentById(componentFromRelease.getComponentId(), header);
        }
        return Optional.of(getComponentByName(componentFromRelease.getName(), header)
                .orElseGet(() -> createComponent(componentFromRelease, header)));
    }

    public SW360Component createComponent(SW360Component component, HttpHeaders header) {
        if(! SW360ComponentAdapterUtils.isValidComponent(component)) {
            throw new SW360ClientException("Can not write invalid component for " + component.getName());
        }
        return componentClient.createComponent(component, header);
    }

    public Optional<SW360Component> getComponentById(String componentId, HttpHeaders header) {
        return componentClient.getComponent(componentId, header);
    }

    public Optional<SW360Component> getComponentByName(String componentName, HttpHeaders header) {
        List<SW360SparseComponent> components = componentClient.searchByName(componentName, header);

        return components.stream()
                .filter(c -> c.getName().equals(componentName))
                .map(c -> SW360HalResourceUtility.getLastIndexOfSelfLink(c.get_Links()).orElse(""))
                .map(id -> getComponentById(id, header))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(c -> c.getName().equals(componentName))
                .findFirst();
    }

    public List<SW360SparseComponent> getComponents(HttpHeaders header) {
        return componentClient.getComponents(header);
    }
}
