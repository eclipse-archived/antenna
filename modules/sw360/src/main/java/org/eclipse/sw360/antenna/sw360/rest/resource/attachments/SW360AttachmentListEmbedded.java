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
package org.eclipse.sw360.antenna.sw360.rest.resource.attachments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(as = SW360AttachmentListEmbedded.class)
public class SW360AttachmentListEmbedded implements Embedded {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("sw360:attachments")
    private List<SW360SparseAttachment> attachments;

    public List<SW360SparseAttachment> getAttachments() {
        return this.attachments;
    }

    public SW360AttachmentListEmbedded setAttachments(List<SW360SparseAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SW360AttachmentListEmbedded that = (SW360AttachmentListEmbedded) o;
        return Objects.equals(attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachments);
    }
}