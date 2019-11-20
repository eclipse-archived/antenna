/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AntennaWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaWorkflow.class);

    private final Collection<AbstractAnalyzer> analyzers;
    private final Collection<AbstractProcessor> processors;
    private final Collection<AbstractGenerator> generators;
    private final List<AbstractOutputHandler> postSinksHooks;

    public AntennaWorkflow(AntennaWorkflowConfiguration antennaWFConfig) {
        LOGGER.debug("Initializing workflow ...");
        analyzers = antennaWFConfig.getAnalyzers();
        processors = antennaWFConfig.getProcessors();
        generators = antennaWFConfig.getGenerators();
        postSinksHooks = antennaWFConfig.getOutputHandlers();
        LOGGER.debug("Initializing workflow done\n");
    }

    public Map<String, IAttachable> execute() {
        LOGGER.info("Workflow execution started ...");
        try {
            LOGGER.debug("Start collecting dependencies from");
            Collection<WorkflowStepResult> sourcesResults = getArtifactsFromAnalyzers();
            ProcessingState processingState = new ProcessingState(sourcesResults);
            if (processingState.getArtifacts().isEmpty()) {
                LOGGER.warn("No analyzer yielded artifacts, skip all other workflow steps");
                return processingState.getAttachables();
            }

            LOGGER.debug("Process artifacts");
            applyProcessors(processingState);

            LOGGER.debug("Generate output");
            Map<String, IAttachable> generatedOutput = generateOutputViaGenerators(processingState);

            generatedOutput.putAll(processingState.getAttachables());

            if(!processingState.getFailCausingResults().isEmpty()) {
                logFailCausingResults(processingState.getFailCausingResults());
                throw new ExecutionException("Build failed due to fail causing results.");
            }

            if(postSinksHooks.size() > 0) {
                LOGGER.debug("Post process output");
                applyOutputPostHandler(generatedOutput);
            }

            return generatedOutput;
        } finally {
            LOGGER.debug("Clean up workflow ...");
            cleanup();
            LOGGER.debug("Clean up workflow done");
            LOGGER.debug("Workflow execution done");
        }
    }

    private void logFailCausingResults(Map<String, Set<IEvaluationResult>> failCausingResults) {
        makeStringForFailCausingResults(failCausingResults).forEach(LOGGER::error);
    }

    private Collection<String> makeStringForFailCausingResults(Map<String, Set<IEvaluationResult>> failCausingResults) {
        Collection<String> resultLines = new ArrayList<>();

        resultLines.add("Build fails due to fail causing results");

        failCausingResults.forEach((workflowItemName, evaluationResults) ->
                createResultLines(resultLines, workflowItemName, evaluationResults));

        return resultLines;
    }

    private void createResultLines(Collection<String> resultLines, String workflowItemName, Set<IEvaluationResult> evaluationResults) {
        evaluationResults.stream()
                .forEach(evalRes -> resultLines.add(String.format("%s: %s", workflowItemName, evalRes.resultAsMessage())));
    }

    private Collection<WorkflowStepResult> getArtifactsFromAnalyzers() {
        Collection<WorkflowStepResult> results = new HashSet<>();
        for(AbstractAnalyzer source : analyzers){
            LOGGER.info("Run {}", source.getWorkflowItemName());
            results.add(source.yield());
        }
        return results;
    }

    private void applyProcessors(ProcessingState processingState) {
        for (AbstractProcessor processor : processors) {
            LOGGER.info("Run {}", processor.getWorkflowItemName());
            processingState.applyWorkflowStepResult(processor.process(processingState));
        }
    }

    private void warnIfKeysCollide(AbstractGenerator sink, Map<String, IAttachable> generatedOutput, Map<String, IAttachable> oneGeneratedOutput) {
        Set<String> collidingKeys = generatedOutput.keySet().stream()
                .filter(oneGeneratedOutput.keySet()::contains)
                .collect(Collectors.toSet());
        if(collidingKeys.size() > 0) {
            LOGGER.warn("The generator " + sink.getWorkflowItemName() + " overwrites the generated output with the keys:\n\t"
                    + String.join(", ", collidingKeys));
        }
    }

    private Map<String, IAttachable> generateOutputViaGenerators(ProcessingState processingState) {
        Map<String, IAttachable> generatedOutput = new HashMap<>();
        for (AbstractGenerator sink : generators) {
            LOGGER.info("Run {}", sink.getWorkflowItemName());
            Map<String, IAttachable> oneGeneratedOutput = sink.produce(processingState);
            warnIfKeysCollide(sink, generatedOutput, oneGeneratedOutput);
            generatedOutput.putAll(oneGeneratedOutput);
        }
        return generatedOutput;
    }

    private void applyOutputPostHandler(Map<String, IAttachable> generatedOutput) {
        for (AbstractOutputHandler postSinksHook: postSinksHooks) {
            postSinksHook.handle(generatedOutput);
        }
    }

    private void cleanup() {
        Stream.of(analyzers, processors, generators, postSinksHooks)
                .flatMap(Collection::stream)
                .forEach(ConfigurableWorkflowItem::cleanup);
    }
}
