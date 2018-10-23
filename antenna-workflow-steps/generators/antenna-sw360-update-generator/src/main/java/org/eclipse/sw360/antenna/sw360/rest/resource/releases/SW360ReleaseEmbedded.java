/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest.resource.releases;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;

import java.util.List;

@JsonDeserialize(as = SW360ReleaseEmbedded.class)
public class SW360ReleaseEmbedded implements Embedded {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("sw360:releases")
    private List<SW360Release> releases;

    public List<SW360Release> getReleases() { return releases; }

    public SW360ReleaseEmbedded setReleases(List<SW360Release> releases) {
        this.releases = releases;
        return this;
    }
}
