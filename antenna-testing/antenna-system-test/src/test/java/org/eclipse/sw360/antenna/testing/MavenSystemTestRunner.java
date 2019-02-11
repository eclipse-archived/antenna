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
package org.eclipse.sw360.antenna.testing;

import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNull;

public class MavenSystemTestRunner extends AbstractSystemTestRunner {

    private boolean debug;

    public MavenSystemTestRunner(boolean debug) {
        this.debug = debug;
    }

    private static final List<String> MVN_GOALS = Arrays.asList("clean", "verify");

    @Override
    public int run(AbstractTestProject testProject) throws MavenInvocationException, IOException, InterruptedException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(testProject.getProjectPom().toFile());
        request.setGoals(MVN_GOALS);
        request.setDebug(this.debug);

        // propagate proxy related properties
        Properties props = new Properties();
        System.getProperties().entrySet().stream().filter(e -> e.getKey().toString().startsWith("proxy"))
                .forEach(e -> props.setProperty(e.getKey().toString(), e.getValue().toString()));
        request.setProperties(props);

        Invoker invoker = new DefaultInvoker();
        InvocationResult ir = invoker.execute(request);
        assertNull("An exception occurred during Maven execution", ir.getExecutionException());
        return ir.getExitCode();
    }
}
