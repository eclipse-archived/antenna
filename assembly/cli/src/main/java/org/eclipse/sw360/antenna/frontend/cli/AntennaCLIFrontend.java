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
package org.eclipse.sw360.antenna.frontend.cli;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIFrontend;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AntennaCLIFrontend extends AbstractAntennaCLIFrontend {

    public AntennaCLIFrontend(File file) {
        super(file);
    }

    @Override
    protected String getPluginDescendantArtifactIdName() {
        return "antenna-maven-plugin";
    }

    public static void main(String[] args) {
        AntennaCLIOptions options = AntennaCLIOptions.parse(args);
        if (options.isShowHelp()) {
            printUsage();
            System.exit(1);
        }

        if (options.isDebugLog()) {
            enableDebugLogging();
        }

        try {
            Path pomFilePath = Paths.get(options.getConfigFilePath()).toAbsolutePath();

            if (!pomFilePath.toFile().exists()) {
                throw new IllegalArgumentException("Cannot find " + pomFilePath.toString());
            }

            AntennaCLIFrontend frontend = new AntennaCLIFrontend(pomFilePath.toFile());
            frontend.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void enableDebugLogging() {
        Configurator.setRootLevel(Level.DEBUG);
        Configurator.setLevel("org.eclipse.sw360.antenna", Level.DEBUG);
    }

    private static void printUsage() {
        System.out.println(AntennaCLIOptions.helpMessage());
    }
}
