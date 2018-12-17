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
package org.eclipse.sw360.antenna.frontend.testProjects;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class TestProjectAsserter {
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            throw new UnsupportedOperationException("Wrong number of arguments, expected 2: ProjectName and PathToTarget");
        }

        String projectName = args[0];
        Path projectPath = Paths.get(args[1]);

        ExecutableTestProject testProject;

        try {
            Class<?> aClass = Class.forName("org.eclipse.sw360.antenna.frontend.testProjects." + projectName);
            if(! Arrays.asList(aClass.getInterfaces()).contains(ExecutableTestProject.class)){
                throw new UnsupportedOperationException("Project name=["+projectName+"] is not an executable test project");
            }
            testProject = (ExecutableTestProject) aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Project name=["+projectName+"] is not supported");
        }

        testProject.assertExecutionResult(projectPath, null, null);

        System.out.println("SUCCESS");
    }
}
