/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Report for processingMessages.
 */
public class Report {
    private final List<ProcessingMessage> messageList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Add the given ProcessingMessage to this report.
     * 
     * @param msg
     */
    public void add(ProcessingMessage msg) {
        synchronized (messageList) {
            messageList.add(msg);
        }
    }

    public List<ProcessingMessage> getMessageList() {
        return messageList;
    }

}
