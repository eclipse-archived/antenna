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
package org.eclipse.sw360.antenna.frontend.stub.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAntennaCLIOptions {
    /**
     * The prefix to identify command line switches.
     */
    public static final String SWITCH_PREFIX = "-";

    /**
     * The short command line switch to enable debug logging.
     */
    public static final String SWITCH_DEBUG_SHORT = SWITCH_PREFIX + "X";

    /**
     * The long command line switch to enable debug logging.
     */
    public static final String SWITCH_DEBUG_LONG = SWITCH_PREFIX + "-debug";

    /**
     * The short command line switch to display a help message.
     */
    public static final String SWITCH_HELP_SHORT = SWITCH_PREFIX + "h";

    /**
     * The long command line switch to display a help message.
     */
    public static final String SWITCH_HELP_LONG = SWITCH_PREFIX + "-help";

    /**
     * Flag whether debug logging should be enabled.
     */
    protected final boolean debugLog;

    /**
     * Flag whether the help message should be printed.
     */
    protected final boolean showHelp;

    /**
     * Flag whether the command line could be parsed successfully.
     */
    protected final boolean valid;

    public AbstractAntennaCLIOptions(boolean debugLog, boolean showHelp, boolean valid) {
        this.debugLog = debugLog;
        this.showHelp = showHelp;
        this.valid = valid;
    }

    /**
     * Returns a flag whether debug log should be enabled.
     *
     * @return a flag whether debug log is desired
     */
    public boolean isDebugLog() {
        return debugLog;
    }

    /**
     * Returns a flag whether the usage help message should be printed. This
     * flag is set when a corresponding command line switch has been detected.
     * In this case, typically no further processing is desired.
     *
     * @return a flag whether the help message is to be printed
     */
    public boolean isShowHelp() {
        return showHelp;
    }

    /**
     * Returns a flag whether the command line options could be validated
     * successfully. Only if this method returns <strong>true</strong>, the
     * other get methods return meaningful values.
     *
     * @return a flag whether the command line is valid
     */
    public boolean isValid() {
        return valid;
    }

    public abstract String toString();

    /**
     * Returns a list with all command line arguments that are interpreted as
     * paths. These are all the arguments that do not start with the prefix for
     * switches.
     *
     * @param args the array with command line options
     * @return a list with all found path arguments
     */
    protected static List<String> readPathsFromArgs(String[] args) {
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
    protected static Set<String> readSwitchesFromArgs(String[] args) {
        return Arrays.stream(args)
                .filter(AbstractAntennaCLIOptions::isSwitch)
                .collect(Collectors.toSet());
    }

    /**
     * Checks whether a command line argument is a switch. Switches start with
     * a specific prefix. All non-switch arguments are interpreted as paths.
     *
     * @param arg the argument to be checked
     * @return <strong>true</strong> if this argument is a switch;
     * <strong>false</strong> otherwise
     */
    protected static boolean isSwitch(String arg) {
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
    protected static boolean hasSwitch(Set<String> switches, String name) {
        return switches.remove(name);
    }

    /**
     * Checks whether there are still unconsumed switches that were not
     * recognized.
     *
     * @param switches the set with remaining switches
     * @return a flag whether there are unsupported switches left
     */
    protected static boolean hasUnsupportedSwitches(Set<String> switches) {
        return !switches.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAntennaCLIOptions that = (AbstractAntennaCLIOptions) o;
        return isDebugLog() == that.isDebugLog() &&
                isShowHelp() == that.isShowHelp() &&
                isValid() == that.isValid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDebugLog(), isShowHelp(), isValid());
    }
}
