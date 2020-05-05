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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractAntennaCLIOptionsTest {
    @Test
    public void testEquals(){
        EqualsVerifier.forClass(AbstractAntennaCLIOptions.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testIsMethods() {
        boolean debugLog = true;
        boolean showHelp = false;
        boolean valid = true;
        AbstractAntennaCLIOptions abstractAntennaCLIOptions = new AbstractAntennaCLIOptions(debugLog, showHelp, valid) {
            @Override
            public String toString() {
                return null;
            }
        };
        assertThat(abstractAntennaCLIOptions.isDebugLog()).isEqualTo(debugLog);
        assertThat(abstractAntennaCLIOptions.isShowHelp()).isEqualTo(showHelp);
        assertThat(abstractAntennaCLIOptions.isValid()).isEqualTo(valid);
    }
}