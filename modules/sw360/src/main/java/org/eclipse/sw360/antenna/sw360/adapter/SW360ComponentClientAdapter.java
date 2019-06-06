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
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SW360ComponentClientAdapter {
    private final SW360ComponentClient componentClient;

    public SW360ComponentClientAdapter(String restUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        this.componentClient = new SW360ComponentClient(restUrl, proxyUse, proxyHost, proxyPort);
    }

    public SW360Component addComponent(Artifact artifact, HttpHeaders header) throws AntennaException {
        SW360Component component = new SW360Component();
        SW360ComponentAdapterUtils.prepareComponent(component, artifact);

        return componentClient.createComponent(component, header);
    }

    public SW360Component getComponentById(String componentId, HttpHeaders header) throws AntennaException {
        return componentClient.getComponent(componentId, header);
    }

    public boolean isArtifactAvailableAsComponent(Artifact artifact, HttpHeaders header) throws AntennaException {
        String componentName = SW360ComponentAdapterUtils.createComponentName(artifact);

        List<SW360SparseComponent> components = componentClient.getComponents(header);

        return components.stream()
                .map(SW360SparseComponent::getName)
                .anyMatch(name -> name.equals(componentName));
    }

    public Optional<SW360Component> getComponentByArtifact(Artifact artifact, HttpHeaders header) throws AntennaException {
        String componentName = SW360ComponentAdapterUtils.createComponentName(artifact);

        return getComponentByName(componentName, header);
    }

    public Optional<SW360Component> getComponentByName(String componentName, HttpHeaders header) throws AntennaException {
        List<SW360Component> completeComponents = new ArrayList<>();
        List<SW360SparseComponent> components = componentClient.searchByName(componentName, header);

        List<String> componentIds = components.stream()
                .filter(c -> c.getName().equals(componentName))
                .map(c -> SW360HalResourceUtility.getLastIndexOfLinkObject(c.get_Links()).orElse(""))
                .collect(Collectors.toList());

        for (String componentId : componentIds) {
            completeComponents.add(getComponentById(componentId, header));
        }

        return completeComponents.stream()
                .filter(c -> c.getName().equals(componentName))
                .findFirst();

    }
}
