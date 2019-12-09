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
package org.eclipse.sw360.antenna.sw360.rest.resources.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.*;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ProjectTests {
    public SW360Project prepareProject() {
        SW360Project sw360Project = new SW360Project();
        sw360Project.setName("Project Name");
        sw360Project.setVersion("1.0.0-SNAPSHOT");
        sw360Project.setCreatedOn("2019-12-09");
        sw360Project.setBusinessUnit("TestUnit");
        sw360Project.setClearingTeam("ClearingUnit");
        sw360Project.setDescription("This is a test project");
        sw360Project.setProjectType(SW360ProjectType.SERVICE);
        sw360Project.setVisibility(SW360Visibility.EVERYONE);
        Map<String, SW360ProjectReleaseRelationship> releaseRelationshipMap = new LinkedHashMap<>();
                releaseRelationshipMap.put("releaseName",
                new SW360ProjectReleaseRelationship(
                        SW360ReleaseRelationship.OPTIONAL,
                        SW360MainlineState.OPEN));
        sw360Project.setReleaseIdToUsage(releaseRelationshipMap);
        return sw360Project;
    }

    @Test
    public void serializationTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final SW360Project release = prepareProject();

        final String jsonBody = objectMapper.writeValueAsString(release);
        final SW360Project deserialized = objectMapper.readValue(jsonBody, SW360Project.class);

        assertThat(deserialized.get_Embedded())
                .isEqualTo(release.get_Embedded());
        assertThat(deserialized)
                .isEqualTo(release);
    }
}
