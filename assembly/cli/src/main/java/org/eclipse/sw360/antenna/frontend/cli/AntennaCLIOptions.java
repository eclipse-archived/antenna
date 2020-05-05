/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.cli;

import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * A class representing the command line options supported by the Antenna CLI
 * and providing simple parsing functionality.
 * </p>
 * <p>
 * This class is used to process the command line passed to the Antenna CLI.
 * It provides getters corresponding to the options supported. It also supports
 * error handling: invalid options can be detected, and a usage message can be
 * printed.
 * </p>
 */
public final class AntennaCLIOptions extends AbstractAntennaCLIOptions {
    /**
     * Constant for an options instance representing an invalid command line.
     * This instance is returned by a failed parse operation.
     */
    private static final AntennaCLIOptions INVALID_OPTIONS =
            new AntennaCLIOptions(null, false, true, false);

    /**
     * The path to the file with the Antenna configuration.
     */
    private final String configFilePath;


    /**
     * Creates a new instance of {@code AntennaCLIOptions} with the properties
     * provided.
     *
     * @param configFilePath the path to the Antenna config file
     * @param debugLog       flag whether debug log should be active
     * @param showHelp       flag whether the help message should be printed
     * @param valid          flag whether the command line is valid
     */
    AntennaCLIOptions(String configFilePath, boolean debugLog, boolean showHelp, boolean valid) {
        super(debugLog, showHelp, valid);
        this.configFilePath = configFilePath;
    }

    /**
     * Returns the path to the Antenna configuration file that has been
     * specified on the command line.
     *
     * @return the path to the Antenna configuration file
     */
    String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * Parses the given command line options and returns a corresponding
     * {@code AntennaCLIOptions} instance. If parsing fails, e.g. if unknown or
     * missing options are detected, the object returned has the
     * {@link #isValid()} flag set to <strong>false</strong>; then the values
     * of the other properties are undefined.
     *
     * @param args the array with command line options
     * @return an {@code AntennaCLIOptions} instance with the result of the
     * parse operation
     */
    static AntennaCLIOptions parse(String[] args) {
        List<String> paths = readPathsFromArgs(args);
        if (paths.size() != 1) {
            return INVALID_OPTIONS;
        }

        Set<String> switches = readSwitchesFromArgs(args);
        boolean debug1 = hasSwitch(switches, SWITCH_DEBUG_SHORT);
        boolean debug2 = hasSwitch(switches, SWITCH_DEBUG_LONG);
        boolean help1 = hasSwitch(switches, SWITCH_HELP_SHORT);
        boolean help2 = hasSwitch(switches, SWITCH_HELP_LONG);
        if (hasUnsupportedSwitches(switches)) {
            return INVALID_OPTIONS;
        }

        return new AntennaCLIOptions(paths.get(0), debug1 || debug2, help1 || help2, true);
    }

    /**
     * Returns a help message that describes the command line options supported
     * by the Antenna CLI.
     *
     * @return the help message
     */
    static String helpMessage() {
        String cr = System.lineSeparator();
        return "Usage: java -jar antenna.jar [options] <pomFilePath>" + cr + cr +
                "Supported options:" + cr +
                SWITCH_HELP_SHORT + ", " + SWITCH_HELP_LONG + ":    Displays this help message." + cr +
                SWITCH_DEBUG_SHORT + ", " + SWITCH_DEBUG_LONG +
                ":   Sets log level to DEBUG for diagnostic purposes." + cr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AntennaCLIOptions that = (AntennaCLIOptions) o;
        return Objects.equals(getConfigFilePath(), that.getConfigFilePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getConfigFilePath());
    }

    @Override
    public String toString() {
        return "AntennaCLIOptions{" +
                "configFilePath='" + configFilePath + '\'' +
                ", debugLog=" + debugLog +
                ", showHelp=" + showHelp +
                ", valid=" + valid +
                '}';
    }
}
