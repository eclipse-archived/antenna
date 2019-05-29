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
package org.eclipse.sw360.antenna.frontend;

import org.eclipse.sw360.antenna.frontend.cli.AbstractAntennaCLIFrontend;
import org.eclipse.sw360.antenna.frontend.cli.AbstractAntennaCLIFrontendTest;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProjectWithExpectations;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.junit.Assume.assumeTrue;

public class AntennaCLIFrontendTest extends AbstractAntennaCLIFrontendTest {

    public AntennaCLIFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        super(testDataSupplier, name);
    }

    @Override
    public AbstractAntennaCLIFrontend getAntennaFrontend(File pomFile) {
        return new AntennaCLIFrontend(pomFile);
    }

    @Test
    public void testExecutionUsingMainMethod()
            throws Exception {
        assumeTrue(runExecutionTest);
        protoypeExecutionTest(() -> {
            Path pom = testData.getProjectPom();
            String[] args = new String[]{ pom.toAbsolutePath().toString() };
            AntennaCLIFrontend.main(args);
        }, tf -> null);
    }
}
