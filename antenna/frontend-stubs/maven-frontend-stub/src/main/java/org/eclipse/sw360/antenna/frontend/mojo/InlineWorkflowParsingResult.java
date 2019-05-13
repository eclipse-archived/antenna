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
package org.eclipse.sw360.antenna.frontend.mojo;

import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

public class InlineWorkflowParsingResult extends Workflow {

    public static class InlineWorkflowStepParsingResult extends WorkflowStep {
        public static class InlineStepConfigurationParsingResult extends StepConfiguration {
            public void setEntry(Entry value) {
                getEntry().add(value);
            }
        }

        public void setConfiguration(InlineStepConfigurationParsingResult value) {
            super.setConfiguration(value);
        }
    }

    public static class InlineAnalyzersParsingResult extends Workflow.Analyzers {
        public void setStep(InlineWorkflowStepParsingResult step){
            getStep().add(step);
        }
    }


    public static class InlineProcessorsParsingResult extends Workflow.Processors {
        public void setStep(InlineWorkflowStepParsingResult step){
            getStep().add(step);
        }
    }


    public static class InlineGeneratorsParsingResult extends Workflow.Generators {
        public void setStep(InlineWorkflowStepParsingResult step){
            getStep().add(step);
        }
    }


    public static class InlineOutputHandlersParsingResult extends Workflow.OutputHandlers {
        public void setStep(InlineWorkflowStepParsingResult step){
            getStep().add(step);
        }
    }

    public void setAnalyzers(InlineAnalyzersParsingResult analyzers) {
        super.setAnalyzers(analyzers);
    }

    public void setProcessors(InlineProcessorsParsingResult processors) {
        super.setProcessors(processors);
    }

    public void setGenerators(InlineGeneratorsParsingResult generators) {
        super.setGenerators(generators);
    }

    public void setOutputHandlers(InlineOutputHandlersParsingResult outputHandlers) {
        super.setOutputHandlers(outputHandlers);
    }
}
