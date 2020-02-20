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

import org.eclipse.sw360.antenna.sw360.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseLinkObjects;
import org.eclipse.sw360.antenna.sw360.utils.SW360ProjectAdapterUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SW360ProjectClientAdapterTest {
    private static final String PROJECT_VERSION = "1.0-projectVersion";
    private static final String PROJECT_NAME = "projectName";
    private final String PROJECT_LAST_INDEX = "12345";

    private SW360ProjectClientAdapter projectClientAdapter;

    private SW360ProjectClient projectClient = mock(SW360ProjectClient.class);

    private HttpHeaders header = mock(HttpHeaders.class);
    private SW360Project projectWithLink;
    private LinkObjects linkObjects;

    @Before
    public void setUp() {
        projectClientAdapter = new SW360ProjectClientAdapter();

        String projectHref = "url/" + PROJECT_LAST_INDEX;
        Self projectSelf = new Self().setHref(projectHref);
        linkObjects = new LinkObjects()
                .setSelf(projectSelf);

        projectWithLink = new SW360Project();
        SW360ProjectAdapterUtils.prepareProject(projectWithLink, PROJECT_NAME, PROJECT_VERSION);
        projectWithLink.set_Links(linkObjects);
    }

    @Test
    public void testGetProjectIdByNameAndVersion() {
        when(projectClient.searchByName(any(), any()))
                .thenReturn(Collections.singletonList(projectWithLink));
        projectClientAdapter.setProjectClient(projectClient);

        Optional<String> projectIdByNameAndVersion = projectClientAdapter.getProjectIdByNameAndVersion(PROJECT_NAME, PROJECT_VERSION, header);

        assertThat(projectIdByNameAndVersion).isPresent();
        assertThat(projectIdByNameAndVersion).hasValue(PROJECT_LAST_INDEX);
    }

    @Test
    public void testAddProject() {
        when(projectClient.createProject(any(), any()))
                .thenReturn(projectWithLink);
        projectClientAdapter.setProjectClient(projectClient);

        String indexOfSelfLink = projectClientAdapter.addProject(PROJECT_NAME, PROJECT_VERSION, header);

        assertThat(indexOfSelfLink).isEqualTo(PROJECT_LAST_INDEX);
    }

    @Test
    public void testAddSW360ReleasesToSW360Project() {
        projectClientAdapter.setProjectClient(projectClient);

        SW360ReleaseLinkObjects releaseLinkObjects = new SW360ReleaseLinkObjects();
        releaseLinkObjects.setSelf(linkObjects.getSelf());
        SW360Release release = new SW360Release();
        release.set_Links(releaseLinkObjects);


        Collection<SW360Release> releases = Collections.singletonList(release);

        projectClientAdapter.addSW360ReleasesToSW360Project(PROJECT_LAST_INDEX, releases, header);

        verify(projectClient, atLeastOnce()).addReleasesToProject(eq(PROJECT_LAST_INDEX), any(), eq(header));
    }
}