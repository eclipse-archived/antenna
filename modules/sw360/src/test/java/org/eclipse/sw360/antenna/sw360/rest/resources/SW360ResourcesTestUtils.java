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
package org.eclipse.sw360.antenna.sw360.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class SW360ResourcesTestUtils<T extends SW360HalResource<?,?>> {

    public abstract T prepareItem();

    public abstract Class<T> getHandledClassType();

    @Test
    public void serializationTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final T release = prepareItem();

        final String jsonBody = objectMapper.writeValueAsString(release);
        final T deserialized = objectMapper.readValue(jsonBody, getHandledClassType());

        assertThat(deserialized.get_Embedded())
                .isEqualTo(release.get_Embedded());
        assertThat(deserialized)
                .isEqualTo(release);
    }
}
