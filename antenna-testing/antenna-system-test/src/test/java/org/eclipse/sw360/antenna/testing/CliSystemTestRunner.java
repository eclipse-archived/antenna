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

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CliSystemTestRunner extends AbstractSystemTestRunner {

    private String getAntennaCliJarPath() {
        final String antennaCliJar = System.getProperty("antenna.cli.jar");
        if (antennaCliJar == null) {
            throw new RuntimeException("Path to antenna cli jar must be available via antenna.cli.jar system property");
        }
        return antennaCliJar;
    }

    @Override
    public int run(AbstractTestProject testProject) throws MavenInvocationException, IOException, InterruptedException {
        final String antennaCliJar = getAntennaCliJarPath();

        List<String> commandLine = new ArrayList<>();
        commandLine.add("java");

        // propagate proxy related properties
        System.getProperties().entrySet().stream().filter(e -> e.getKey().toString().startsWith("proxy"))
                .forEach(e -> commandLine.add(String.format("-D%s=%s", e.getKey().toString(), e.getValue().toString())));

        commandLine.add("-jar");
        commandLine.add(antennaCliJar);
        commandLine.add(testProject.getProjectPom().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.directory(testProject.projectRoot.toFile());
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();

        // Write stdout (that also contains sterr) of subprocess to our
        // stdout
        final InputStream inStream = p.getInputStream();
        new Thread(new Runnable() {
            private Scanner scan;

            @Override
            public void run() {
                InputStreamReader reader = new InputStreamReader(inStream);
                scan = new Scanner(reader);
                while (scan.hasNextLine()) {
                    System.out.println(scan.nextLine());
                }
            }
        }).start();

        p.waitFor(3, TimeUnit.MINUTES);
        return p.exitValue();
    }
}
