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
package org.eclipse.sw360.antenna.sw360.utils;


import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ComponentAdapterUtilsTest {
    @Test
    public void testSetComponentTypeProprietary() {
        SW360Component component = new SW360Component();
        component.setName("test");

        SW360ComponentAdapterUtils.setComponentType(component, true);

        assertThat(component.getComponentType()).isEqualTo(SW360ComponentType.INTERNAL);
    }

    @Test
    public void testSetComponentTypeNonProprietary() {
        SW360Component component = new SW360Component();
        component.setName("test");

        SW360ComponentAdapterUtils.setComponentType(component, false);

        assertThat(component.getComponentType()).isEqualTo(SW360ComponentType.OSS);

    }

    @Test
    public void testCreateFromRelease() {
        SW360Release release = new SW360Release()
                .setName("test")
                .setProprietary(true);

        SW360Component component = SW360ComponentAdapterUtils.createFromRelease(release);

        assertThat(component.getName()).isEqualTo(release.getName());
        assertThat(component.getComponentType()).isEqualTo(SW360ComponentType.INTERNAL);
    }

    @Test
    public void testIsValidComponentWithValidComponent() {
        SW360Component component = new SW360Component();
        component.setName("test");

        boolean validComponent = SW360ComponentAdapterUtils.isValidComponent(component);

        assertThat(validComponent).isTrue();

    }

    @Test
    public void testIsValidComponentWithInvalidComponent() {
        SW360Component component = new SW360Component();

        boolean validComponent = SW360ComponentAdapterUtils.isValidComponent(component);

        assertThat(validComponent).isFalse();

    }
}