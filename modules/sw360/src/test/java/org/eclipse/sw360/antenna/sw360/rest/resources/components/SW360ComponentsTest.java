/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest.resources.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ComponentsTest {
    public SW360Component prepareComponent() {
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("Component Name");
        sw360Component.setComponentType(SW360ComponentType.COTS);
        sw360Component.setHomepage("componentName.org");
        sw360Component.setCreatedOn("2019-12-09");
        return sw360Component;
    }

    @Test
    public void serializationTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final SW360Component release = prepareComponent();

        final String jsonBody = objectMapper.writeValueAsString(release);
        final SW360Component deserialized = objectMapper.readValue(jsonBody, SW360Component.class);

        assertThat(deserialized.get_Embedded())
                .isEqualTo(release.get_Embedded());
        assertThat(deserialized)
                .isEqualTo(release);
    }
}
