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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AntennaComplianceToolOptionsTest {
    private static final String PROPERTIES_PATH = "path/to/compliancetool.properties";

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
        AntennaComplianceToolOptions options1 = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter", true, false, true);
        checkEquals(options1, options1, true);

        AntennaComplianceToolOptions options2 = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter", true, false, true);
        checkEquals(options1, options2, true);
    }

    @Test
    public void testEqualsFalse() {
        AntennaComplianceToolOptions options1 = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter", true, false, true);
        AntennaComplianceToolOptions options2 = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter" + "_other", true, false, true);
        checkEquals(options1, options2, false);

        options2 = new AntennaComplianceToolOptions(options1.getPropertiesFilePath(), AntennaComplianceToolOptions.SWITCH_EXPORTER_SHORT, false, false, true);
        checkEquals(options1, options2, false);

        options2 = new AntennaComplianceToolOptions(options1.getPropertiesFilePath(), AntennaComplianceToolOptions.SWITCH_EXPORTER_SHORT, true, false, false);
        checkEquals(options1, options2, false);

        options2 = new AntennaComplianceToolOptions(options1.getPropertiesFilePath(), AntennaComplianceToolOptions.SWITCH_EXPORTER_SHORT, true, true, true);
        checkEquals(options1, options2, false);

        options1 = options2;
        options1 = new AntennaComplianceToolOptions(options1.getPropertiesFilePath(), AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, true, true, true);
        checkEquals(options1, options2, false);
    }

    @Test
    public void testEqualsCornerCases() {
        AntennaComplianceToolOptions options = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter", false, false, true);

        checkEquals(options, this, false);
        checkEquals(options, null, false);
    }

    @Test
    public void testToString() {
        AntennaComplianceToolOptions options = new AntennaComplianceToolOptions(PROPERTIES_PATH, "exporter", true, false, true);
        String s = options.toString();

        assertThat(s).contains("propertiesFilePath='" + options.getPropertiesFilePath());
        assertThat(s).contains("complianceMode=" + options.getComplianceMode());
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
    private static void checkParse(AntennaComplianceToolOptions expOptions, String... args) {
        AntennaComplianceToolOptions options = AntennaComplianceToolOptions.parse(args);

        assertThat(options).isEqualTo(expOptions);
    }

    /**
     * Helper method to invoke a parse operation which is expected to fail.
     *
     * @param args the command line to be parsed
     */
    private static void checkFailedParse(String... args) {
        AntennaComplianceToolOptions options = AntennaComplianceToolOptions.parse(args);

        assertThat(options.isValid()).isFalse();
        assertThat(options.isShowHelp()).isTrue();
    }

    @Test
    public void testParseWithMultiplePaths() {
        checkFailedParse(PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, "another/path");
    }

    @Test
    public void testParseWithNoPath() {
        checkFailedParse(AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT);
    }

    @Test
    public void testParseWithUnknownSwitch() {
        checkFailedParse(PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, "--unknown");
    }

    @Test
    public void testParseWithTwoModes() {
        checkFailedParse(PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_EXPORTER_SHORT, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, "--unknown");
    }

    @Test
    public void testParseWithNoModes() {
        checkFailedParse(PROPERTIES_PATH, "--unknown");
    }

    @Test
    public void testParseWithXSwitch() {
        checkParse(new AntennaComplianceToolOptions(PROPERTIES_PATH, "updater", true, false, true),
                PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, "-X");
    }

    @Test
    public void testParseWithDebugSwitch() {
        checkParse(new AntennaComplianceToolOptions(PROPERTIES_PATH, "updater", true, false, true),
                AntennaComplianceToolOptions.SWITCH_DEBUG_LONG, PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT);
    }

    @Test
    public void testParseWithMultipleDebugSwitches() {
        checkParse(new AntennaComplianceToolOptions(PROPERTIES_PATH, "updater", true, false, true),
                AntennaComplianceToolOptions.SWITCH_DEBUG_LONG, AntennaComplianceToolOptions.SWITCH_DEBUG_SHORT, PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT);
    }

    @Test
    public void testParseWithShortHelpSwitch() {
        checkParse(new AntennaComplianceToolOptions(PROPERTIES_PATH, "updater", false, true, true),
                PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, "-h");
    }

    @Test
    public void testParseWithLongHelpSwitch() {
        checkParse(new AntennaComplianceToolOptions(PROPERTIES_PATH, "updater",false, true, true),
                "--help", PROPERTIES_PATH, AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT);
    }

    @Test
    public void testHelpMessage() {
        List<String> expectedFragments = Arrays.asList("Usage: ", AntennaComplianceToolOptions.SWITCH_DEBUG_LONG,
                AntennaComplianceToolOptions.SWITCH_DEBUG_SHORT, AntennaComplianceToolOptions.SWITCH_HELP_LONG,
                AntennaComplianceToolOptions.SWITCH_HELP_SHORT, "[options]", "<propertiesFilePath>",
                "Compliance Tool modes: (only one can be set)",
                AntennaComplianceToolOptions.SWITCH_EXPORTER_SHORT, AntennaComplianceToolOptions.SWITCH_EXPORTER_LONG,
                AntennaComplianceToolOptions.SWITCH_UPDATER_SHORT, AntennaComplianceToolOptions.SWITCH_UPDATER_LONG);

        String helpMessage = AntennaComplianceToolOptions.helpMessage();
        assertThat(helpMessage).contains(expectedFragments);
        System.out.println(helpMessage);
    }
}