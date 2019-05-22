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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AntennaBasicCLIFrontend extends AbstractAntennaCLIFrontend {

    public AntennaBasicCLIFrontend(File file) {
        super(file);
    }

    @Override
    protected String getPluginDescendantArtifactIdName() {
        return "basic-maven-plugin";
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            System.exit(1);
        }

        try {
            Path pomFilePath = Paths.get(args[0]).toAbsolutePath();

            if (!pomFilePath.toFile().exists()) {
                throw new IllegalArgumentException("Cannot find " + pomFilePath.toString());
            }

            AntennaBasicCLIFrontend frontend = new AntennaBasicCLIFrontend(pomFilePath.toFile());
            frontend.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }


    private static void printUsage() {
        System.out.println("Usage: java -jar antenna.jar <pomFilePath>");
    }
}
