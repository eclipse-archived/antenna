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

import org.eclipse.sw360.antenna.frontend.mojo.AbstractAntennaMojoFrontendTest;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProjectWithExpectations;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class AntennaBasicMojoFrontendTest extends AbstractAntennaMojoFrontendTest {

    public AntennaBasicMojoFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        super(testDataSupplier, name);
    }

    @Override
    public void checkBooleans() {
        // maven is always used regardless of what the project specifies, so we shouldn't compare those
        assertEquals(testData.getExpectedToolConfigurationAttachAll(), antennaContext.getToolConfiguration().isAttachAll());
        assertEquals(testData.getExpectedToolConfigurationSkip(), antennaContext.getToolConfiguration().isSkipAntennaExecution());
    }
}
