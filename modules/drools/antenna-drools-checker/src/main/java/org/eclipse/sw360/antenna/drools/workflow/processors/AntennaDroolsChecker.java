/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.drools.workflow.processors;

import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.drools.DroolsEngine;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.workflow.processors.checkers.AbstractComplianceChecker;

import java.util.*;

public class AntennaDroolsChecker extends AbstractComplianceChecker {
    private final DroolsEngine droolsEngine = new DroolsEngine();

    private static final String BASEDIR_KEY = "base.dir";
    private static final String POLICIES_FOLDER_PATH = "folder.paths";
    private static final String NO_VERSION = "no version string specified";

    public AntennaDroolsChecker() {
        this.workflowStepOrder = VALIDATOR_BASE_ORDER + 10000;
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);
        droolsEngine.setRulesetDirectory(getConfigValue(BASEDIR_KEY, configMap));
        droolsEngine.setDebug(context.getDebug());

        StringTokenizer tokenizer = new StringTokenizer(getConfigValue(POLICIES_FOLDER_PATH, configMap), ";");
        List<String> folderList = new ArrayList<>();
        while(tokenizer.hasMoreTokens()) {
            folderList.add(tokenizer.nextToken());
        }

        droolsEngine.setRulesetPaths(folderList);
        droolsEngine.setTemporaryDirectory(context.getToolConfiguration().getAntennaTargetDirectory().resolve("temporaryRules"));
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) throws AntennaException {
        return droolsEngine.evaluate(artifacts);
    }

    @Override
    public WorkflowStepResult postProcessResult(WorkflowStepResult result) {
        result.addAdditionalReportComment("Evaluated with Antenna Drools Engine, rules and policies version ["
                + droolsEngine.getRulesetVersion().orElse(NO_VERSION) + "]");
        return result;
    }

    @Override
    public String getRulesetDescription() {
        return "Antenna Drools Engine, rules and policies version [" +
                droolsEngine.getRulesetVersion().orElse(NO_VERSION) + "]";
    }
}
