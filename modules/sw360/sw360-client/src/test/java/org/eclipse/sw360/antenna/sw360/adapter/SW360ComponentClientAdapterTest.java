/*
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
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SW360ComponentClientAdapterTest {
    private final static String COMPONENT_ID = "12345";
    private final static String COMPONENT_NAME = "componentName";

    private SW360ComponentClientAdapter componentClientAdapter;

    private SW360ComponentClient componentClient = mock(SW360ComponentClient.class);
    private HttpHeaders header = mock(HttpHeaders.class);

    private SW360SparseComponent sparseComponent;
    private SW360Component component;

    @Before
    public void setUp() {
        componentClientAdapter = new SW360ComponentClientAdapter();
        componentClientAdapter.setComponentClient(componentClient);
        sparseComponent = new SW360SparseComponent();
        component = new SW360Component();
    }

    @Test
    public void testGetOrCreateComponentByID() {
        SW360Component componentFromRelease = mock(SW360Component.class);
        when(componentFromRelease.getComponentId()).thenReturn(COMPONENT_ID);
        when(componentClient.getComponent(COMPONENT_ID, header)).thenReturn(Optional.of(component));

        Optional<SW360Component> optResult = componentClientAdapter.getOrCreateComponent(componentFromRelease, header);
        assertThat(optResult).contains(component);
    }

    @Test
    public void testGetOrCreateComponentByName() {
        SW360Component componentFromRelease = mock(SW360Component.class);
        when(componentFromRelease.getComponentId()).thenReturn(null);
        when(componentFromRelease.getName()).thenReturn(COMPONENT_NAME);
        LinkObjects linkObjects = makeLinkObjects();
        sparseComponent.setName(COMPONENT_NAME)
                .set_Links(linkObjects);
        component.setName(COMPONENT_NAME);

        when(componentClient.getComponent(COMPONENT_ID, header))
                .thenReturn(Optional.of(component));
        when(componentClient.searchByName(COMPONENT_NAME, header))
                .thenReturn(Collections.singletonList(sparseComponent));

        Optional<SW360Component> optResult = componentClientAdapter.getOrCreateComponent(componentFromRelease, header);
        assertThat(optResult).contains(component);
    }

    @Test
    public void testGetOrCreateComponentCreateNew() {
        SW360Component componentFromRelease = mock(SW360Component.class);
        when(componentFromRelease.getComponentId()).thenReturn(null);
        when(componentFromRelease.getName()).thenReturn(COMPONENT_NAME);
        when(componentClient.searchByName(COMPONENT_NAME, header)).thenReturn(Collections.emptyList());
        when(componentClient.createComponent(componentFromRelease, header)).thenReturn(component);

        Optional<SW360Component> optResult = componentClientAdapter.getOrCreateComponent(componentFromRelease, header);
        assertThat(optResult).contains(component);
    }

    @Test
    public void testCreateComponent() {
        component.setName(COMPONENT_NAME);
        when(componentClient.createComponent(component, header))
                .thenReturn(component);

        SW360Component createdComponent = componentClientAdapter.createComponent(this.component, header);

        assertThat(createdComponent).isEqualTo(component);
        verify(componentClient).createComponent(component, header);
    }

    @Test (expected = SW360ClientException.class)
    public void testCreateComponentNull() {
        when(componentClient.createComponent(component, header))
                .thenReturn(component);

        componentClientAdapter.createComponent(this.component, header);
    }

    @Test
    public void testGetComponentById() {
        when(componentClient.getComponent(COMPONENT_ID, header))
                .thenReturn(Optional.of(component));

        Optional<SW360Component> componentById = componentClientAdapter.getComponentById(COMPONENT_ID, header);

        assertThat(componentById).isPresent();
        assertThat(componentById).hasValue(component);
        verify(componentClient).getComponent(COMPONENT_ID, header);
    }

    @Test
    public void testGetComponentByName() {
        LinkObjects linkObjects = makeLinkObjects();
        sparseComponent.setName(COMPONENT_NAME)
                .set_Links(linkObjects);

        component.setName(COMPONENT_NAME);

        when(componentClient.getComponent(COMPONENT_ID, header))
                .thenReturn(Optional.of(component));
        when(componentClient.searchByName(COMPONENT_NAME, header))
                .thenReturn(Collections.singletonList(sparseComponent));

        Optional<SW360Component> componentByName = componentClientAdapter.getComponentByName(COMPONENT_NAME, header);

        assertThat(componentByName).isPresent();
        assertThat(componentByName).hasValue(component);
        verify(componentClient).getComponent(COMPONENT_ID, header);
        verify(componentClient).searchByName(COMPONENT_NAME, header);
    }

    @Test
    public void testGetComponents() {
        when(componentClient.getComponents(header))
                .thenReturn(Collections.singletonList(sparseComponent));

        List<SW360SparseComponent> components = componentClientAdapter.getComponents(header);

        assertThat(components).hasSize(1);
        assertThat(components).containsExactly(sparseComponent);
        verify(componentClient).getComponents(header);
    }

    private LinkObjects makeLinkObjects() {
        String componentHref = "url/" + COMPONENT_ID;
        Self componentSelf = new Self().setHref(componentHref);
        return new LinkObjects()
                .setSelf(componentSelf);
    }
}