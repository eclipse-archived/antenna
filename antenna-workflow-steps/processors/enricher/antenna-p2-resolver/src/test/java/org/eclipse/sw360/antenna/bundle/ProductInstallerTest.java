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

package org.eclipse.sw360.antenna.bundle;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductInstallerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testProductInstallation() throws IOException, AntennaException {
        ProductInstaller productInstaller = new ProductInstaller();
        File extractionPath = temporaryFolder.newFolder();
        productInstaller.installEclipseProductForP2Resolution(extractionPath.toString());

        // The product contains an eclipse launcher file which is just "eclipse"
        assertThat(OperatingSystemSpecifics.getEclipseExecutable(extractionPath)).exists();
    }

}
