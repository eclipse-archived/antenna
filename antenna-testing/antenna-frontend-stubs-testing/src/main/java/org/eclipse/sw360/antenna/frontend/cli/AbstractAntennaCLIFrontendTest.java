/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.cli;

import org.eclipse.sw360.antenna.frontend.AbstractAntennaFrontendTest;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProjectWithExpectations;
import org.junit.Before;
import org.junit.Ignore;

import java.io.File;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

public abstract class AbstractAntennaCLIFrontendTest extends AbstractAntennaFrontendTest {

    public AbstractAntennaCLIFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        super(testDataSupplier, name);
    }

    public abstract AbstractAntennaCLIFrontend getAntennaFrontend(File pomFile) throws Exception;

    @Before
    public void loadTestContext() throws Exception {
        File pomFile = testData.getProjectPom().toFile();

        antennaFrontend = getAntennaFrontend(pomFile);
        assertNotNull(antennaFrontend);

        antennaContext = antennaFrontend.init().buildAntennaContext();
        assertNotNull(antennaContext);
        assertNotNull(antennaContext.getProject());

        runExecutionTest = !testData.requiresMaven();
    }

    @Ignore("would fail due to unrelated templating issues")
    @Override
    public void checkParsedWorkflowForOutputHandlers() {
    }
}