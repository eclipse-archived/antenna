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

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import java.nio.file.Path;
import java.util.Optional;

public class SW360Attachment extends SW360HalResource<LinkObjects, Embedded> {
    private final String filename;
    private final String attachmentType;

    public SW360Attachment(Path path, String attachmentType) {
        this(Optional.ofNullable(path)
                .map(Path::getFileName)
                .orElseThrow(() -> new AntennaExecutionException("Tried to add null path.")).toString(),
                attachmentType);
    }

    public SW360Attachment(String filename, String attachmentType) {
        this.filename = Optional.ofNullable(filename)
                .orElseThrow(() -> new AntennaExecutionException("Filename is not allowed to be null."));
        this.attachmentType = attachmentType;
    }

    public SW360Attachment(String filename) {
        this(filename, "DOCUMENT");
    }

    public String getFilename() {
        return filename;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        SW360Attachment that = (SW360Attachment) object;
        return filename.equals(that.filename) &&
                java.util.Objects.equals(attachmentType, that.attachmentType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), filename, attachmentType);
    }
}
