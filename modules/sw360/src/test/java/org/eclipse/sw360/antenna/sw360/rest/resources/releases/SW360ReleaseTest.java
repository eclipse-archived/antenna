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
package org.eclipse.sw360.antenna.sw360.rest.resources.releases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ReleaseTest {
    public SW360Release prepareRelease() {
        SW360Release release = new SW360Release();
        release.setName("Release Name");
        release.setVersion("1.2.3");
        release.setCopyrights("Copyright 2019 SuperCorp");
        release.setHashes(Collections.singleton("UIADRETDNDUIAGFOEHNTR"));
        release.setChangeStatus("AS_IS");
        release.setReleaseId("RELEASE_ID");
        release.setComponentId("COMPONENT_ID");
        release.setMainLicenseIds(Stream.of("MIT","BSD-3-Clause").collect(Collectors.toSet()));
        return release;
    }

    @Test
    public void testReleaseMergeWithNonDominantNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();
        sw360Release1.setCpeId(null);
        sw360Release2.setCpeId("cpe:ishere");

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeId()).isEqualTo(sw360Release2.getCpeId());
    }

    @Test
    public void testReleaseMergeWithBothNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();

        sw360Release1.setCpeId(null);
        sw360Release2.setCpeId(null);

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeId()).isEqualTo(sw360Release2.getCpeId());
    }

    @Test
    public void testReleaseMergeWithDominantNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();

        sw360Release1.setCpeId("cpe:ishere");
        sw360Release2.setCpeId(null);

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeId()).isNotEqualTo(sw360Release2.getCpeId());
    }

    @Test
    public void equalsTest() {
        assertThat(prepareRelease())
                .isEqualTo(prepareRelease());
    }

    @Test
    public void serializationTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final SW360Release release = prepareRelease();

        final String jsonBody = objectMapper.writeValueAsString(release);
        final SW360Release deserialized = objectMapper.readValue(jsonBody, SW360Release.class);

        assertThat(deserialized.get_Embedded())
                .isEqualTo(release.get_Embedded());
        assertThat(deserialized)
                .isEqualTo(release);
    }
}
