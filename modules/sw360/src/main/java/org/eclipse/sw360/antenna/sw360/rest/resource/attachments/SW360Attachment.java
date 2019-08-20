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

import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import java.nio.file.Path;

public class SW360Attachment extends SW360HalResource<LinkObjects, Embedded> {
    private final String filename;
    private final String attachmentType;

    public SW360Attachment(Path path, String attachmentType) {
        this(path.getFileName().toString(), attachmentType);
    }

    public SW360Attachment(String filename, String attachmentType) {
        this.filename = filename;
        this.attachmentType = attachmentType;
    }

    public SW360Attachment(String filename) {
        this.filename = filename;
        this.attachmentType = "DOCUMENT";
    }

    public String getFilename() {
        return filename;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

}
