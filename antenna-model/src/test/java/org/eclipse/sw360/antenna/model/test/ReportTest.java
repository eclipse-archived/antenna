/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ReportTest {

    @Test
    public void test() {
        Report report = new Report();
        ProcessingMessage msg = new ProcessingMessage(MessageType.PROCESSING_FAILURE);
        report.add(msg);
        assertThat(report.getMessageList().get(0)).isEqualTo(msg);
    }

}
