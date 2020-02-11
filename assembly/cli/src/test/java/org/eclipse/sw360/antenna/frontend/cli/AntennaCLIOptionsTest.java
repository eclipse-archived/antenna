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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AntennaCLIOptionsTest {
    private static final String CONFIG_PATH = "path/to/config.xml";

    /**
     * Helper method to check whether the equals() method returns the expected
     * result. Hash codes are checked as well.
     *
     * @param obj1      object 1
     * @param obj2      object 2
     * @param expResult the expected result of equals()
     */
    private static void checkEquals(Object obj1, Object obj2, boolean expResult) {
        assertThat(obj1.equals(obj2)).isEqualTo(expResult);
        if (obj2 != null) {
            assertThat(obj2.equals(obj1)).isEqualTo(expResult);  // symmetry
            if (expResult) {
                assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode());
            }
        }
    }

    @Test
    public void testEqualsTrue() {
        AntennaCLIOptions options1 = new AntennaCLIOptions(CONFIG_PATH, true, false, true);
        checkEquals(options1, options1, true);

        AntennaCLIOptions options2 = new AntennaCLIOptions(CONFIG_PATH, true, false, true);
        checkEquals(options1, options2, true);
    }

    @Test
    public void testEqualsFalse() {
        AntennaCLIOptions options1 = new AntennaCLIOptions(CONFIG_PATH, true, false, true);
        AntennaCLIOptions options2 = new AntennaCLIOptions(CONFIG_PATH + "_other", true, false, true);
        checkEquals(options1, options2, false);

        options2 = new AntennaCLIOptions(options1.getConfigFilePath(), false, false, true);
        checkEquals(options1, options2, false);

        options2 = new AntennaCLIOptions(options1.getConfigFilePath(), true, false, false);
        checkEquals(options1, options2, false);

        options2 = new AntennaCLIOptions(options1.getConfigFilePath(), true, true, true);
        checkEquals(options1, options2, false);
    }

    @Test
    public void testEqualsCornerCases() {
        AntennaCLIOptions options = new AntennaCLIOptions(CONFIG_PATH, false, false, true);

        checkEquals(options, this, false);
        checkEquals(options, null, false);
    }

    @Test
    public void testToString() {
        AntennaCLIOptions options = new AntennaCLIOptions(CONFIG_PATH, true, false, true);
        String s = options.toString();

        assertThat(s).contains("configFilePath='" + options.getConfigFilePath());
        assertThat(s).contains("debugLog=" + options.isDebugLog());
        assertThat(s).contains("showHelp=" + options.isShowHelp());
        assertThat(s).contains("valid=" + options.isValid());
    }

    /**
     * Helper method for testing parse operations. This method creates an
     * options instance from the given command line options and compares it
     * against the expectations provided.
     *
     * @param expOptions the expected options
     * @param args       the command line to be parsed
     */
    private static void checkParse(AntennaCLIOptions expOptions, String... args) {
        AntennaCLIOptions options = AntennaCLIOptions.parse(args);

        assertThat(options).isEqualTo(expOptions);
    }

    /**
     * Helper method to invoke a parse operation which is expected to fail.
     *
     * @param args the command line to be parsed
     */
    private static void checkFailedParse(String... args) {
        AntennaCLIOptions options = AntennaCLIOptions.parse(args);

        assertThat(options.isValid()).isFalse();
        assertThat(options.isShowHelp()).isTrue();
    }

    @Test
    public void testParseNoArguments() {
        checkFailedParse();
    }

    @Test
    public void testParseConfigPathOnly() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, false, false, true), CONFIG_PATH);
    }

    @Test
    public void testParseWithXSwitch() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, true, false, true), CONFIG_PATH, "-X");
    }

    @Test
    public void testParsePositionOfOptionsDoesNotMatter() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, true, false, true), "-X", CONFIG_PATH);
    }

    @Test
    public void testParseWithMultiplePaths() {
        checkFailedParse(CONFIG_PATH, "another/path");
    }

    @Test
    public void testParseWithUnknownSwitch() {
        checkFailedParse(CONFIG_PATH, "--unknown");
    }

    @Test
    public void testParseWithDebugSwitch() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, true, false, true), "--debug", CONFIG_PATH);
    }

    @Test
    public void testParseWithMultipleDebugSwitches() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, true, false, true),
                AntennaCLIOptions.SWITCH_DEBUG_LONG, CONFIG_PATH, AntennaCLIOptions.SWITCH_DEBUG_SHORT);
    }

    @Test
    public void testParseWithShortHelpSwitch() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, true, true, true),
                CONFIG_PATH, "-X", "-h");
    }

    @Test
    public void testParseWithLongHelpSwitch() {
        checkParse(new AntennaCLIOptions(CONFIG_PATH, false, true, true),
                "--help", CONFIG_PATH);
    }

    @Test
    public void testHelpMessage() {
        List<String> expectedFragments = Arrays.asList("Usage: ", AntennaCLIOptions.SWITCH_DEBUG_LONG,
                AntennaCLIOptions.SWITCH_DEBUG_SHORT, AntennaCLIOptions.SWITCH_HELP_LONG,
                AntennaCLIOptions.SWITCH_HELP_SHORT, "[options]", "<pomFilePath>");

        String helpMessage = AntennaCLIOptions.helpMessage();
        assertThat(helpMessage).contains(expectedFragments);
        System.out.println(helpMessage);
    }
}
