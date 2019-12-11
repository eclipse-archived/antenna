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
package org.eclipse.sw360.antenna.sw360.rest.resources.attachments;

import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resources.SW360ResourcesTestUtils;

public class SW360AttachmentTest extends SW360ResourcesTestUtils {
    @Override
    public SW360Attachment prepareItem() {
        SW360Attachment sw360Attachment = new SW360Attachment("test.doc", SW360AttachmentType.SOURCE);
        return sw360Attachment;
    }

    @Override
    public Class<SW360Attachment> getHandledClassType() {
        return SW360Attachment.class;
    }
}
