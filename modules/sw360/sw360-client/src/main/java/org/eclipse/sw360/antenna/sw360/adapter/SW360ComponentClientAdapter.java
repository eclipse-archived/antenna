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

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;

import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.block;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.optionalFuture;

public class SW360ComponentClientAdapter {
    private final SW360ComponentClient componentClient;

    public SW360ComponentClientAdapter(SW360ComponentClient client) {
        componentClient = client;
    }

    public Optional<SW360Component> getOrCreateComponent(SW360Component componentFromRelease) {
        if(componentFromRelease.getComponentId() != null) {
            return getComponentById(componentFromRelease.getComponentId());
        }
        return Optional.of(getComponentByName(componentFromRelease.getName())
                .orElseGet(() -> createComponent(componentFromRelease)));
    }

    public SW360Component createComponent(SW360Component component) {
        if(!SW360ComponentAdapterUtils.isValidComponent(component)) {
            throw new SW360ClientException("Can not write invalid component for " + component.getName());
        }
        return block(componentClient.createComponent(component));
    }

    public Optional<SW360Component> getComponentById(String componentId) {
        return block(optionalFuture(componentClient.getComponent(componentId)));
    }

    public Optional<SW360Component> getComponentByName(String componentName) {
        List<SW360SparseComponent> components = block(componentClient.searchByName(componentName));

        return components.stream()
                .filter(c -> c.getName().equals(componentName))
                .map(c -> SW360HalResourceUtility.getLastIndexOfSelfLink(c.get_Links()).orElse(""))
                .map(this::getComponentById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(c -> c.getName().equals(componentName))
                .findFirst();
    }

    public List<SW360SparseComponent> getComponents() {
        return block(componentClient.getComponents());
    }
}
