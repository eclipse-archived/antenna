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
package org.eclipse.sw360.antenna.model.util;

import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class WorkflowComparator {

    public static boolean areEqual(StepConfiguration configuration1, StepConfiguration configuration2) {
        if (configuration1 == null && configuration2 == null) {
            return true;
        }
        if (configuration1 == null) {
            return false;
        }
        if (configuration2 == null) {
            return false;
        }

        final Map<String, String> map1 = configuration1.getAsMap();
        final Map<String, String> map2 = configuration2.getAsMap();
        return map1.size() == map2.size() &&
                map1.entrySet().stream()
                        .allMatch(entry1 -> entry1.getValue().equals(map2.get(entry1.getKey())));
    }

    public static boolean areEqual(List<WorkflowStep> steps1, List<WorkflowStep> steps2) {
        return steps1.size() == steps2.size() &&
                steps1.stream()
                        .allMatch(step1 -> steps2.stream()
                                .filter(step2 -> step1.getName().equals(step2.getName()))
                                .filter(step2 -> step1.getClassHint() == null && step2.getClassHint() == null ||
                                        step1.getClassHint() != null && step1.getClassHint().equals(step2.getClassHint()))
                                .filter(step2 -> Optional.ofNullable(step1.isDeactivated()).orElse(false)
                                        .equals(Optional.ofNullable(step2.isDeactivated()).orElse(false)))
                                .filter(step2 -> areEqual(step2.getConfiguration(), step1.getConfiguration()))
                                .count() == 1);
    }

    private static boolean checkStepForEqual(Workflow workflow1, Workflow workflow2,
                                      Function<Workflow,Object> firstGetter, Function<Workflow,List<WorkflowStep>> secondGetter) {
        return firstGetter.apply(workflow1) == null && firstGetter.apply(workflow2) == null ||
                firstGetter.apply(workflow1) != null && areEqual(secondGetter.apply(workflow1), secondGetter.apply(workflow2));
    }

    public static boolean areEqual(Workflow workflow1, Workflow workflow2) {
        return checkStepForEqual(workflow1, workflow2, Workflow::getAnalyzers, w -> w.getAnalyzers().getStep()) &&
                checkStepForEqual(workflow1, workflow2, Workflow::getProcessors, w -> w.getProcessors().getStep()) &&
                checkStepForEqual(workflow1, workflow2, Workflow::getGenerators, w -> w.getGenerators().getStep()) &&
                checkStepForEqual(workflow1, workflow2, Workflow::getOutputHandlers, w -> w.getOutputHandlers().getStep());
    }

}
