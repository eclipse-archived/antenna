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
package org.eclipse.sw360.antenna.frontend.compliancetool.main;

import java.util.*;
import java.util.stream.Collectors;

public class AntennaComplianceToolOptions {
    /**
     * The prefix to identify command line switches.
     */
    private static final String SWITCH_PREFIX = "-";

    /**
     * The short command line switch to enable debug logging.
     */
    private static final String SWITCH_DEBUG_SHORT = SWITCH_PREFIX + "X";

    /**
     * The long command line switch to enable debug logging.
     */
    private static final String SWITCH_DEBUG_LONG = SWITCH_PREFIX + "-debug";

    /**
     * The short command line switch to display a help message.
     */
    private static final String SWITCH_HELP_SHORT = SWITCH_PREFIX + "h";

    /**
     * The long command line switch to display a help message.
     */
    private static final String SWITCH_HELP_LONG = SWITCH_PREFIX + "-help";

    /**
     * The short command line switch to execute the updater
     */
    private static final String SWITCH_UPDATER_SHORT = SWITCH_PREFIX + "u";

    /**
     * The long command line switch to execute the updater
     */
    private static final String SWITCH_UPDATER_LONG = SWITCH_PREFIX + "-updater";

    /**
     * The short command line switch to execute the exporter
     */
    private static final String SWITCH_EXPORTER_SHORT = SWITCH_PREFIX + "e";

    /**
     * The long command line switch to execute the exporter
     */
    private static final String SWITCH_EXPORTER_LONG = SWITCH_PREFIX + "-exporter";

    /**
     * Constant for an options instance representing an invalid command line.
     * This instance is returned by a failed parse operation.
     */
    private static final AntennaComplianceToolOptions INVALID_OPTIONS =
            new AntennaComplianceToolOptions(null, null, false, true, false);

    /**
     * The path to the file with the Antenna configuration.
     */
    private final String propertiesFilePath;

    /**
     * Flag whether debug logging should be enabled.
     */
    private final boolean debugLog;

    /**
     * Flag whether the help message should be printed.
     */
    private final boolean showHelp;

    /**
     * Flag whether the command line could be parsed successfully.
     */
    private final boolean valid;

    /**
     * The mode the compliance tool will get executed with.
     */
    private final String complianceMode;

    /**
     * Creates a new instance of {@code AntennaComplianceToolOptions} with the properties
     * provided.
     *
     * @param propertiesFilePath the path to the Antenna config file
     * @param complianceMode     the mode the compliance tool will get executed with
     * @param debugLog           flag whether debug log should be active
     * @param showHelp           flag whether the help message should be printed
     * @param valid              flag whether the command line is valid
     */
    private AntennaComplianceToolOptions(String propertiesFilePath, String complianceMode, boolean debugLog, boolean showHelp, boolean valid) {
        this.propertiesFilePath = propertiesFilePath;
        this.complianceMode = complianceMode;
        this.debugLog = debugLog;
        this.showHelp = showHelp;
        this.valid = valid;
    }

    /**
     * Parses the given command line options and returns a corresponding
     * {@code AntennaComplianceToolOptions} instance. If parsing fails, e.g. if unknown or
     * missing options are detected, the object returned has the
     * {@link #isValid()} flag set to <strong>false</strong>; then the values
     * of the other properties are undefined.
     *
     * @param args the array with command line options
     * @return an {@code AntennaComplianceToolOptions} instance with the result of the
     * parse operation
     */
    static AntennaComplianceToolOptions parse(String[] args) {
        List<String> paths = readPathsFromArgs(args);
        if (paths.size() != 1) {
            return INVALID_OPTIONS;
        }

        Set<String> switches = readSwitchesFromArgs(args);
        boolean debug1 = hasSwitch(switches, SWITCH_DEBUG_SHORT);
        boolean debug2 = hasSwitch(switches, SWITCH_DEBUG_LONG);
        boolean help1 = hasSwitch(switches, SWITCH_HELP_SHORT);
        boolean help2 = hasSwitch(switches, SWITCH_HELP_LONG);
        Optional<String> complianceModeFromSwitches = getComplianceModeFromSwitches(switches);

        if (hasUnsupportedSwitches(switches) || !complianceModeFromSwitches.isPresent()) {
            return INVALID_OPTIONS;
        }

        return new AntennaComplianceToolOptions(paths.get(0), complianceModeFromSwitches.get(), debug1 || debug2, help1 || help2, true);
    }

    /**
     * Returns the path to the Antenna configuration file that has been
     * specified on the command line.
     *
     * @return the path to the Antenna configuration file
     */
    String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    /**
     * Returns the mode the compliance tool will be executed with that
     * has been specified by the command line.
     *
     * @return the mode the compliance tool will be executed with.
     */
    String getComplianceMode() {
        return complianceMode;
    }

    /**
     * Returns a flag whether debug log should be enabled.
     *
     * @return a flag whether debug log is desired
     */
    boolean isDebugLog() {
        return debugLog;
    }

    /**
     * Returns a flag whether the usage help message should be printed. This
     * flag is set when a corresponding command line switch has been detected.
     * In this case, typically no further processing is desired.
     *
     * @return a flag whether the help message is to be printed
     */
    boolean isShowHelp() {
        return showHelp;
    }

    /**
     * Returns a flag whether the command line options could be validated
     * successfully. Only if this method returns <strong>true</strong>, the
     * other get methods return meaningful values.
     *
     * @return a flag whether the command line is valid
     */
    private boolean isValid() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AntennaComplianceToolOptions options = (AntennaComplianceToolOptions) o;
        return isDebugLog() == options.isDebugLog() &&
                isShowHelp() == options.isShowHelp() &&
                isValid() == options.isValid() &&
                Objects.equals(getPropertiesFilePath(), options.getPropertiesFilePath()) &&
                Objects.equals(getComplianceMode(), options.getComplianceMode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPropertiesFilePath(), isDebugLog(), isShowHelp(), isValid());
    }

    @Override
    public String toString() {
        return "AntennaComplianceToolOptions{" +
                "propertiesFilePath='" + propertiesFilePath + '\'' +
                ", debugLog=" + debugLog +
                ", showHelp=" + showHelp +
                ", valid=" + valid +
                '}';
    }

    /**
     * Returns a help message that describes the command line options supported
     * by the Antenna CLI.
     *
     * @return the help message
     */
    static String helpMessage() {
        String cr = System.lineSeparator();
        return "Usage: java -jar compliancetool.jar [options] <complianceMode> <propertiesFilePath>" + cr + cr +
                "Supported options:" + cr +
                SWITCH_HELP_SHORT + ", " + SWITCH_HELP_LONG + ":    Displays this help message." + cr +
                SWITCH_DEBUG_SHORT + ", " + SWITCH_DEBUG_LONG +
                ":   Sets log level to DEBUG for diagnostic purposes." + cr + cr +
                "Compliance Tool modes: (only one can be set)" + cr +
                SWITCH_EXPORTER_SHORT + ", " + SWITCH_EXPORTER_LONG + ":    Sets the compliance tool execute the SW360Exporter" + cr +
                SWITCH_EXPORTER_SHORT + ", " + SWITCH_EXPORTER_LONG + ":    Sets the compliance tool execute the SW360Updater" + cr;
    }

    /**
     * Returns a list with all command line arguments that are interpreted as
     * paths. These are all the arguments that do not start with the prefix for
     * switches.
     *
     * @param args the array with command line options
     * @return a list with all found path arguments
     */
    private static List<String> readPathsFromArgs(String[] args) {
        return Arrays.stream(args)
                .filter(arg -> !isSwitch(arg))
                .collect(Collectors.toList());
    }

    /**
     * Returns a set with all command line switches that have been provided on
     * the command line. Switches start with a prefix and control the behavior
     * of the Antenna tool.
     *
     * @param args the array with command line options
     * @return a set with all the switches found on the command line
     */
    private static Set<String> readSwitchesFromArgs(String[] args) {
        return Arrays.stream(args)
                .filter(AntennaComplianceToolOptions::isSwitch)
                .collect(Collectors.toSet());
    }

    /**
     * Returns an optional string with the compliance mode found in the
     * command line or null if there was no or too many modes specified.
     *
     * @param switches set with all switches found in the command line.
     * @return an {@code Optional} with the compliance mode found
     * in the command line or null if there was no or too many modes specified.
     */
    private static Optional<String> getComplianceModeFromSwitches(Set<String> switches) {
        boolean updater = hasSwitch(switches, SWITCH_UPDATER_SHORT) || hasSwitch(switches, SWITCH_UPDATER_LONG);
        boolean exporter = hasSwitch(switches, SWITCH_EXPORTER_SHORT) || hasSwitch(switches, SWITCH_EXPORTER_LONG);

        if (updater && exporter) {
            return Optional.empty();
        } else if (updater) {
            return Optional.of("updater");
        } else if (exporter) {
            return Optional.of("exporter");
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks whether a command line argument is a switch. Switches start with
     * a specific prefix. All non-switch arguments are interpreted as paths.
     *
     * @param arg the argument to be checked
     * @return <strong>true</strong> if this argument is a switch;
     * <strong>false</strong> otherwise
     */
    private static boolean isSwitch(String arg) {
        return arg.startsWith(SWITCH_PREFIX);
    }

    /**
     * Checks whether a specific switch has been provided on the command line.
     * The switch is then removed to mark it as consumed.
     *
     * @param switches the set with command line switches
     * @param name     the name of the switch in question
     * @return a flag whether this switch was found
     */
    private static boolean hasSwitch(Set<String> switches, String name) {
        return switches.remove(name);
    }

    /**
     * Checks whether there are still unconsumed switches that were not
     * recognized.
     *
     * @param switches the set with remaining switches
     * @return a flag whether there are unsupported switches left
     */
    private static boolean hasUnsupportedSwitches(Set<String> switches) {
        return !switches.isEmpty();
    }
}
