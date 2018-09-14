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
package org.eclipse.sw360.antenna.frontend.testProjects;

import org.eclipse.sw360.antenna.model.xml.generated.ObjectFactory;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestProjectUtils {

    public static WorkflowStep mkWorkflowStep(String name, String classHint) {
        WorkflowStep step = new WorkflowStep();
        step.setName(name);
        step.setClassHint(classHint);
        return step;
    }

    public static WorkflowStep mkDeactivatedWorkflowStep(String name, String classHint) {
        WorkflowStep ws = mkWorkflowStep(name, classHint);
        ws.setDeactivated(true);
        return ws;
    }

    public static WorkflowStep mkWorkflowStep(String name, String classHint, String key, String value) {
        return mkWorkflowStep(name, classHint, Collections.singletonMap(key, value));
    }

    public static WorkflowStep mkWorkflowStep(String name, String classHint, String key1, String value1, String key2, String value2) {
        Map<String,String> configMap = new HashMap<>();
        configMap.put(key1, value1);
        configMap.put(key2, value2);
        return mkWorkflowStep(name, classHint, configMap);
    }

    public static WorkflowStep mkWorkflowStep(String name, String classHint, Map<String, String> configMap) {
        WorkflowStep step = mkWorkflowStep(name, classHint);
        step.setConfiguration(StepConfiguration.fromMap(configMap));
        return step;
    }
}
