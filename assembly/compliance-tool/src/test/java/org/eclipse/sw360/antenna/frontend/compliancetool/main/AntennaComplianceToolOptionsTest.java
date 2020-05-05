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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AntennaComplianceToolOptionsTest {
    private static final String PROPERTIES_PATH = "path/to/compliancetool.properties";

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AntennaComplianceToolOptions.class)
                .usingGetClass()
                .verify();
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