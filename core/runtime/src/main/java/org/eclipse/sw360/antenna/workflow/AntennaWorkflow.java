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
        LOGGER.info("Initializing workflow ...");
        analyzers = antennaWFConfig.getAnalyzers();
        processors = antennaWFConfig.getProcessors();
        generators = antennaWFConfig.getGenerators();
        postSinksHooks = antennaWFConfig.getOutputHandlers();
        LOGGER.info("Initializing workflow done\n");
    }

    public Map<String, IAttachable> execute() throws ExecutionException {
        LOGGER.info("Workflow execution started ...");
        try {
            LOGGER.info("Start collecting dependencies from");
            Collection<WorkflowStepResult> sourcesResults = getArtifactsFromAnalyzers();
            ProcessingState processingState = new ProcessingState(sourcesResults);
            if (processingState.getArtifacts().isEmpty()) {
                LOGGER.warn("No analyzer yielded artifacts, skip all other workflow steps");
                return processingState.getAttachables();
            }

            LOGGER.info("Process artifacts");
            applyProcessors(processingState);

            LOGGER.info("Generate output");
            Map<String, IAttachable> generatedOutput = generateOutputViaGenerators(processingState);

            generatedOutput.putAll(processingState.getAttachables());

            if(postSinksHooks.size() > 0) {
                LOGGER.info("Post process output");
                applyOutputPostHandler(generatedOutput);
            }

            LOGGER.info("Workflow execution done");
            return generatedOutput;
        } catch (ExecutionException tee) {
            String msg = "Workflow execution failed!";
            LOGGER.error(msg, tee);
            throw tee;
        } finally {
            LOGGER.info("Clean up workflow ...");
            cleanup();
            LOGGER.info("Clean up workflow done");
        }
    }

    private Collection<WorkflowStepResult> getArtifactsFromAnalyzers() {
        Collection<WorkflowStepResult> results = new HashSet<>();
        for(AbstractAnalyzer source : analyzers){
            LOGGER.info("\nCollecting dependencies from source {}", source.getWorkflowItemName());
            results.add(source.yield());
        }
        return results;
    }

    private void applyProcessors(ProcessingState processingState) {
        for (AbstractProcessor processor : processors) {
            LOGGER.info("Let {} process dependencies", processor.getWorkflowItemName());
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
            LOGGER.info("Let {} generate output", sink.getWorkflowItemName());
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
