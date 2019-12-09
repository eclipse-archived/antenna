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
package org.eclipse.sw360.antenna.sw360.rest.resources.licenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360LicenseTest {
    public SW360License prepareLicense() {
        SW360License sw360License = new SW360License();
        sw360License.setShortName("Test-2.0");
        sw360License.setFullName("Test License 2.0");
        sw360License.setText("Full License Text");
        return sw360License;
    }

    @Test
    public void serializationTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final SW360License release = prepareLicense();

        final String jsonBody = objectMapper.writeValueAsString(release);
        final SW360License deserialized = objectMapper.readValue(jsonBody, SW360License.class);

        assertThat(deserialized.get_Embedded())
                .isEqualTo(release.get_Embedded());
        assertThat(deserialized)
                .isEqualTo(release);
    }
}
