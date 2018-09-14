/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.model.util.WorkflowComparator;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(Enclosed.class)
public class WorkflowFileLoaderTest {

    private static class TestWorkflowGenerator {
        static final String xmlStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

        public static String wrapWorkflow(String xml) {
            return xmlStart+"<workflow>\n"+xml+"</workflow>\n";
        }

        public static String xmlWithAnalyzers() {
            return wrapWorkflow("");
        }

        public static String xmlWithAnalyzers(List<String> analyzerNames){
            return xmlWithAnalyzers(analyzerNames, null);
        }

        public static String xmlWithAnalyzers(List<String> analyzerNames, List<String> analyzerHints){
            return xmlWithAnalyzers(analyzerNames, analyzerHints, null);
        }

        public static String xmlWithAnalyzers(List<String> analyzerNames, List<String> analyzerHints, Map<String,String> firstStepConfiguration){
            return xmlWithAnalyzers(analyzerNames, analyzerHints, firstStepConfiguration, "");
        }

        public static String xmlWithAnalyzers(List<String> analyzerNames, List<String> analyzerHints, Map<String,String> firstStepConfiguration, String more){
            StringBuilder xml = new StringBuilder("<analyzers>\n");
            for(int i = 0; i < analyzerNames.size(); i++){
                xml.append("<step>\n");
                xml.append("<name>")
                        .append(analyzerNames.get(i))
                        .append("</name>\n");
                if(analyzerHints != null && analyzerHints.size() > i && analyzerHints.get(i) != null){
                    xml.append("<classHint>")
                            .append(analyzerHints.get(i))
                            .append("</classHint>\n");
                }
                if(i == 0 && firstStepConfiguration != null) {
                    xml.append("<configuration>\n");
                    for (Map.Entry confItem: firstStepConfiguration.entrySet()) {
                        int typeOfEntryRepresentation = 3;
                        switch (typeOfEntryRepresentation) {
                            case 1:
                                xml.append("<entry key=\"")
                                        .append(confItem.getKey())
                                        .append("\" value=\"")
                                        .append(confItem.getValue())
                                        .append("\"/>\n");
                                break;
                            case 2:
                                xml.append("<entry><entryKey>")
                                        .append(confItem.getKey())
                                        .append("</entryKey><entryValue>")
                                        .append(confItem.getValue())
                                        .append("</entryValue></entry>\n");
                                break;
                            case 3:
                                xml.append("<entry key=\"")
                                        .append(confItem.getKey())
                                        .append("\"><entryValue>")
                                        .append(confItem.getValue())
                                        .append("</entryValue></entry>\n");
                                break;
                        }
                    }
                    xml.append("</configuration>\n");
                }
                xml.append("</step>\n");
            }
            xml.append("</analyzers>\n");
            xml.append(more);
            return wrapWorkflow(xml.toString());
        }
    }

    public static class WorflowFileLoaderUnparameterizedTests {
        @Test
        public void testParsingTrivialString() {
            String xml = TestWorkflowGenerator.wrapWorkflow("");

            Workflow workflow = WorkflowFileLoader.loadRenderedWorkflow(xml);

            assertNotNull(workflow);
            assertTrue(workflow.getAnalyzers() == null || workflow.getAnalyzers().getStep().size() == 0);
            assertTrue(workflow.getProcessors() == null || workflow.getProcessors().getStep().size() == 0);
            assertTrue(workflow.getGenerators() == null || workflow.getGenerators().getStep().size() == 0);
        }

        @Test
        public void testParsingComplexString() {
            String xml = TestWorkflowGenerator.xmlWithAnalyzers(
                    Stream.of("Some Analyzer 1", "Some Analyzer 2", "Some Other Analyzer").collect(Collectors.toList()),
                    Stream.of("SomeAnalyzer1", "SomeAnalyzer2").collect(Collectors.toList())
            );

            Workflow workflow = WorkflowFileLoader.loadRenderedWorkflow(xml);

            assertNotNull(workflow);
            assertEquals(3, workflow.getAnalyzers().getStep().size());
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Analyzer 1".equals(s.getName())));
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .filter(s -> "Some Analyzer 1".equals(s.getName()))
                    .anyMatch(s -> "SomeAnalyzer1".equals(s.getClassHint())));
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Analyzer 2".equals(s.getName())));
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Other Analyzer".equals(s.getName())));

            assertTrue(workflow.getProcessors() == null || workflow.getProcessors().getStep().size() == 0);

            assertTrue(workflow.getGenerators() == null || workflow.getGenerators().getStep().size() == 0);
        }

        @Test
        public void testParsingMoreComplexString() {
            String xml = TestWorkflowGenerator.xmlWithAnalyzers(
                    Stream.of("Some Analyzer 1", "Some Analyzer 2", "Some Other Analyzer").collect(Collectors.toList()),
                    Stream.of("SomeAnalyzer1", "SomeAnalyzer2").collect(Collectors.toList()),
                    Collections.singletonMap("configKey", "configValue"),
                    "<processors><step><name>Some Processor</name></step></processors>"
            );

            Workflow workflow = WorkflowFileLoader.loadRenderedWorkflow(xml);

            assertNotNull(workflow);
            assertEquals(3, workflow.getAnalyzers().getStep().size());
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Analyzer 1".equals(s.getName())));
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .filter(s -> "Some Analyzer 1".equals(s.getName()))
                    .anyMatch(s -> "SomeAnalyzer1".equals(s.getClassHint())));
            workflow.getAnalyzers().getStep().stream()
                    .filter(s -> "Some Analyzer 1".equals(s.getName()))
                    .forEach(a -> {
                        assertNotNull(a.getConfiguration());
                        assertEquals(1, a.getConfiguration().getAsMap().size());
                        assertEquals("configValue", a.getConfiguration().getAsMap()
                                .entrySet().stream()
                                .filter(e -> "configKey".equals(e.getKey()))
                                .findAny()
                                .get()
                                .getValue());
                    });
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Analyzer 2".equals(s.getName())));
            assertTrue(workflow.getAnalyzers().getStep().stream()
                    .anyMatch(s -> "Some Other Analyzer".equals(s.getName())));

            assertEquals(1, workflow.getProcessors().getStep().size());

            assertTrue(workflow.getGenerators() == null || workflow.getGenerators().getStep().size() == 0);
        }
    }

    @RunWith(Parameterized.class)
    public static class WorflowFileLoaderParameterizedTests {
        private String classPathWorkflowString;
        private String workflowOverrideString;
        private String expectedWorkflowString;

        @Parameterized.Parameters(name = "{index}: a=[{0}] b=[{1}] c=[{2}]")
        public static Collection<Object[]> data() {
            String xmlStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            return Arrays.asList(new Object[][] {
                    //##################################################################################################
                    // no inner tags
                    {"<workflow></workflow>", "<workflow></workflow>", xmlStart+"<workflow/>"},
                    {xmlStart+"<workflow></workflow>", "<workflow></workflow>", xmlStart+"<workflow/>"},
                    {"<workflow></workflow>", xmlStart+"<workflow></workflow>", xmlStart+"<workflow/>"},
                    {xmlStart+"<workflow></workflow>", xmlStart+"<workflow></workflow>", xmlStart+"<workflow/>"},
                    {"<workflow/>", "<workflow></workflow>", xmlStart+"<workflow/>"},
                    {"<workflow></workflow>", "<workflow/>", xmlStart+"<workflow/>"},
                    {"<workflow/>", "<workflow/>", xmlStart+"<workflow/>"},

                    //##################################################################################################
                    // no steps
                    {"<workflow><analyzers></analyzers></workflow>", "<workflow></workflow>", xmlStart+"<workflow><analyzers/></workflow>"},
                    {"<workflow></workflow>", "<workflow><analyzers></analyzers></workflow>", xmlStart+"<workflow><analyzers/></workflow>"},

                    //##################################################################################################
                    // some analyzers

                    { // analyzers in both
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer").collect(Collectors.toList())
                            ),
                    },

                    { // analyzers in both
                            TestWorkflowGenerator.xmlWithAnalyzers(),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer").collect(Collectors.toList())
                            ),
                    },

                    { // analyzers in both
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Other Analyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer").collect(Collectors.toList())
                            ),
                    },

                    //##################################################################################################
                    // some analyzers with hints
                    { // hint in classpath workflow
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer", "Some Fourth Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer", "SomeOtherAnalyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Other Analyzer", "Some Third Analyzer", "Some Fith Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeOtherNewAnalyzer", "SomeOtherAnalyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer", "Some Fourth Analyzer", "Some Fith Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer", "SomeOtherNewAnalyzer", "SomeOtherAnalyzer").collect(Collectors.toList())
                            ),
                    },

                    { // configuration in override
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey","configValue")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey","configValue")
                            ),
                    },

                    //##################################################################################################
                    // some analyzers with configuration
                    { // configuration in classpath
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer").collect(Collectors.toList()),
                                    null,
                                    Collections.singletonMap("configKey","configValue")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList())
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey","configValue")
                            ),
                    },

                    { // same configuration in classpath and override: override
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer").collect(Collectors.toList()),
                                    null,
                                    Collections.singletonMap("configKey","configValue")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey","configValueNew")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey","configValueNew")
                            )
                    },

                    { // different configuration in classpath and override
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer").collect(Collectors.toList()),
                                    null,
                                    Collections.singletonMap("configKey","configValue")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Collections.singletonMap("configKey2","configValue2")
                            ),
                            TestWorkflowGenerator.xmlWithAnalyzers(
                                    Stream.of("Some Analyzer", "Some Other Analyzer", "Some Third Analyzer").collect(Collectors.toList()),
                                    Stream.of("SomeAnalyzer").collect(Collectors.toList()),
                                    Arrays.stream(new String[][] {
                                            {"configKey","configValue"},
                                            {"configKey2","configValue2"},
                                    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]))
                            )
                    },

            });
        }

        public WorflowFileLoaderParameterizedTests(String classPathWorkflow, String workflowOverride, String expectedWorkflow) {
            this.classPathWorkflowString = classPathWorkflow;
            this.workflowOverrideString = workflowOverride;
            this.expectedWorkflowString = expectedWorkflow;
        }

        private void trimAndCleanupWorkflow(Workflow workflow) {
            if(workflow.getAnalyzers() != null){
                if(workflow.getAnalyzers().getStep().size() == 0){
                    workflow.setAnalyzers(null);
                }
            }
            if(workflow.getProcessors() != null){
                if(workflow.getProcessors().getStep().size() == 0){
                    workflow.setProcessors(null);
                }
            }
            if(workflow.getGenerators() != null){
                if(workflow.getGenerators().getStep().size() == 0){
                    workflow.setGenerators(null);
                }
            }
        }

        private void compareOverridenWorkflows() {
            Workflow classPathWorkflow = WorkflowFileLoader.loadRenderedWorkflow(classPathWorkflowString);
            Workflow workflowOverride = WorkflowFileLoader.loadRenderedWorkflow(workflowOverrideString);
            Workflow expectedWorkflow = WorkflowFileLoader.loadRenderedWorkflow(expectedWorkflowString);

            WorkflowFileLoader.overrideWorkflow(classPathWorkflow, workflowOverride);

            trimAndCleanupWorkflow(classPathWorkflow);
            trimAndCleanupWorkflow(expectedWorkflow);

            assertTrue("The merged workflow does not match the expected one, parsed from:\n" + expectedWorkflowString,
                    WorkflowComparator.areEqual(expectedWorkflow, classPathWorkflow));

            //##########################################################################################################
            // some smoke tests to prevent passing tests due to empty parsing results
            smokeTest(classPathWorkflow);
        }

        private void smokeTest(Workflow workflow) {
            if (expectedWorkflowString.contains("configKey")) {
                int numberOfConfigs = 0;
                if (workflow.getAnalyzers() != null) {
                    numberOfConfigs += workflow.getAnalyzers().getStep().stream()
                            .filter(a -> a.getConfiguration() != null)
                            .mapToLong(a -> a.getConfiguration().getAsMap().entrySet()
                                    .stream()
                                    .filter(e -> e.getKey() != null)
                                    .filter(e -> e.getValue() != null)
                                    .count())
                            .sum();
                }
                if (workflow.getProcessors() != null) {
                    numberOfConfigs += workflow.getProcessors().getStep().stream()
                            .filter(a -> a.getConfiguration() != null)
                            .mapToLong(a -> a.getConfiguration().getAsMap().entrySet()
                                    .stream()
                                    .filter(e -> e.getKey() != null)
                                    .filter(e -> e.getValue() != null)
                                    .count())
                            .sum();
                }
                if (workflow.getGenerators() != null) {
                    numberOfConfigs += workflow.getGenerators().getStep().stream()
                            .filter(a -> a.getConfiguration() != null)
                            .mapToLong(a -> a.getConfiguration().getAsMap().entrySet()
                                    .stream()
                                    .filter(e -> e.getKey() != null)
                                    .filter(e -> e.getValue() != null)
                                    .count())
                            .sum();
                }
                assertTrue(numberOfConfigs > 0);
            }
            if (expectedWorkflowString.contains("Some")) {
                int numberOfSteps = 0;
                if (workflow.getAnalyzers() != null) {
                    numberOfSteps += workflow.getAnalyzers().getStep().stream()
                            .filter(a -> a.getName() != null)
                            .filter(a -> !"".equals(a.getName()))
                            .count();
                }
                if (workflow.getProcessors() != null) {
                    numberOfSteps += workflow.getProcessors().getStep().stream()
                            .filter(a -> a.getName() != null)
                            .filter(a -> !"".equals(a.getName()))
                            .count();
                }
                if (workflow.getGenerators() != null) {
                    numberOfSteps += workflow.getGenerators().getStep().stream()
                            .filter(a -> a.getName() != null)
                            .filter(a -> !"".equals(a.getName()))
                            .count();
                }
                assertTrue(numberOfSteps > 0);
            }
        }

        @Test
        public void compareOverridenWorkflowsForAnalyzers() {
            compareOverridenWorkflows();
        }

        @Test
        public void compareOverridenWorkflowsForPrcessors() {
            replaceAnalyzerWith("generator");
            compareOverridenWorkflows();
        }

        @Test
        public void compareOverridenWorkflowsForGenerators() {
            replaceAnalyzerWith("processor");
            compareOverridenWorkflows();
        }

        private void replaceAnalyzerWith(String string) {
            String stringWithFirstUC = Character.toUpperCase(string.charAt(0)) + string.substring(1);
            Function<String,String> f = s -> s.replace("analyzer",string).replace("Analyzer", stringWithFirstUC);
            classPathWorkflowString = f.apply(classPathWorkflowString);
            workflowOverrideString = f.apply(workflowOverrideString);
            expectedWorkflowString = f.apply(expectedWorkflowString);
        }
    }
}
