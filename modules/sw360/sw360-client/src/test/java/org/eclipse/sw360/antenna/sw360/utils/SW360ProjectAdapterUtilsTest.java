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

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class SW360ProjectAdapterUtilsTest {

    private static final String PROJECT_VERSION = "1.0-projectVersion";
    private static final String PROJECT_NAME = "projectName";

    @Test
    public void testPrepareProject() {
        SW360Project project = new SW360Project();

        String projectName = "projectName";
        String projectVersion = PROJECT_VERSION;

        SW360ProjectAdapterUtils.prepareProject(project, projectName, projectVersion);

        assertThat(project.getName()).isEqualTo(projectName);
        assertThat(project.getVersion()).isEqualTo(projectVersion);
        assertThat(project.getDescription()).isEqualTo(projectName + " " + projectVersion);
        assertThat(project.getProjectType()).isEqualTo(SW360ProjectType.PRODUCT);
        assertThat(project.getVisibility()).isEqualTo(SW360Visibility.BUISNESSUNIT_AND_MODERATORS);
    }

    @Test
    public void testIsValidProjectWithValidProject() {
        SW360Project project = new SW360Project()
                .setName(PROJECT_NAME)
                .setVersion(PROJECT_VERSION);

        boolean validComponent = SW360ProjectAdapterUtils.isValidProject(project);

        assertThat(validComponent).isTrue();
    }

    @Test
    public void testIsValidProjectWithInvalidProject() {
        SW360Project project = new SW360Project();

        boolean validComponent = SW360ProjectAdapterUtils.isValidProject(project);

        assertThat(validComponent).isFalse();
    }
}