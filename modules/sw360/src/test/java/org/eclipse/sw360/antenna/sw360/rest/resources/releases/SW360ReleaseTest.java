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

import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resources.SW360ResourcesTestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ReleaseTest extends SW360ResourcesTestUtils<SW360Release> {
    @Override
    public SW360Release prepareItem() {
        SW360Release release = new SW360Release();
        release.setName("Release Name");
        release.setVersion("1.2.3");
        release.setCopyrights("Copyright 2019 SuperCorp");
        release.setHashes(Collections.singleton("UIADRETDNDUIAGFOEHNTR"));
        release.setChangeStatus("AS_IS");
        release.setReleaseId("RELEASE_ID");
        release.setComponentId("COMPONENT_ID");
        release.setMainLicenseIds(Stream.of("MIT","BSD-3-Clause").collect(Collectors.toSet()));
        ArrayList<SW360SparseAttachment> sparseAttachmentList = new ArrayList<>();
        sparseAttachmentList.add(new SW360SparseAttachment().setFilename("").setAttachmentType(SW360AttachmentType.SOURCE));
        release.get_Embedded().setAttachments(sparseAttachmentList);
        release.setSw360ClearingState(SW360ClearingState.NEW_CLEARING);
        release.setClearingState("INITIAL");
        return release;
    }

    @Override
    public SW360Release prepareItemWithoutOptionalInput() {
        SW360Release release = new SW360Release();
        release.setName("Release Name");
        release.setVersion("1.2.3");
        release.setComponentId("COMPONENT_ID");
        return release;
    }

    @Override
    public Class<SW360Release> getHandledClassType() {
        return SW360Release.class;
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
}
