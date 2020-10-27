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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClearingReportGenerator {
    private static final String CLEARING_DOC_SUFFIX = "_clearing.json";

    Path createClearingDocument(SW360Release release, Path targetDir) {
        String clearingDocumentName = release.getName() + "_" + release.getVersion() + CLEARING_DOC_SUFFIX;
        Path clearingDocument = targetDir.resolve(clearingDocumentName.replaceAll("[/\\\\]", "_"));
        try {
            Files.createDirectories(targetDir);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Files.newBufferedWriter(clearingDocument), release);
            return clearingDocument;
        } catch (IOException e) {
            throw new SW360ClientException("Could not create clearing document " + clearingDocument.toString(), e);
        }
    }
}
