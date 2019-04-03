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

package org.eclipse.sw360.antenna.drools;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class InternalRulesPackage implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalRulesPackage.class);
    private File extractionLocation;

    public InternalRulesPackage(File extractionLocation) {
        this.extractionLocation = extractionLocation;
    }

    @Override
    public void close() {
        try {
            FileUtils.forceDelete(extractionLocation);
        } catch (IOException e) {
            LOGGER.error("Could not delete temporary folder " + extractionLocation.toString() + ".");
        }
    }
}
