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
package org.eclipse.sw360.antenna.util;

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateRendererTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private String property1key = "property1";
    private String property1value = "property1value";
    private String property2key = "property2";
    private String property2value = "property2value";
    private String property3key = "property3";
    private String property3value = "property3value";
    private String propertyWithDotKey = "property.subproperty";
    private String propertyWithDotValue = "propertyValue";
    private TemplateRenderer tr;
    private Map<String, String> properties0 = new HashMap<>();
    private Map<String, String> properties1 = new HashMap<>();
    private Map<String, String> properties2 = new HashMap<>();
    private Map<String, String> properties1and2 = new HashMap<>();
    private Map<String, String> properties1and3 = new HashMap<>();
    private Map<String, String> properties2and3 = new HashMap<>();

    private String exampleInnerTemplate = "<tests>\n"
            + "<test" + property1key + ">$" + property1key + "</test" + property1key + ">\n"
            + "<test" + property1key + ">${" + property1key + "}</test" + property1key + ">\n"
            + "<test" + property2key + ">$" + property2key + "</test" + property2key + ">\n"
            + "</tests>";

    private String exampleDotTemplate = "<tests>\n"
            + "<test" + propertyWithDotValue + ">$" + propertyWithDotKey + "</test" + propertyWithDotValue + ">\n"
            + "<test" + propertyWithDotValue + ">${" + propertyWithDotKey + "}</test" + propertyWithDotValue + ">\n"
            + "<test" + propertyWithDotValue + ">$" + propertyWithDotKey + "</test" + propertyWithDotValue + ">\n"
            + "</tests>";

    @Before
    public void before() {
        tr = new TemplateRenderer();

        properties1.put(property1key, property1value);
        properties2.put(property2key, property2value);
        properties1and2.put(property1key, property1value);
        properties1and2.put(property2key, property2value);
        properties1and3.put(property1key, property1value);
        properties1and3.put(property3key, property3value);
        properties2and3.put(property2key, property2value);
        properties2and3.put(property3key, property3value);
    }

    private TemplateRenderer mkTemplateRendererWithPropertiesMap(Map<String, String> properties) {
        return new TemplateRenderer(properties.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Object) e.getValue())));
    }

    private String addPropertiesToTemplate(String innerTemplate, Map<String, String> properties) {
        String propertiesXml = properties.entrySet().stream()
                .map(e -> "<" + e.getKey() + ">" + e.getValue() + "</" + e.getKey() + ">")
                .reduce("", (s1, s2) -> s1 + "\n" + s2);
        return "<properties>\n" + propertiesXml + "\n</properties>\n" + innerTemplate;
    }

    private String wrapTemplateXml(String innerTemplate) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>\n" + innerTemplate + "\n</project>";
    }

    private File writeTemplateFromString(String template) throws IOException {
        final File templateFile = folder.newFile("template.xml");
        FileUtils.writeStringToFile(templateFile, template, StandardCharsets.UTF_8);
        return templateFile;
    }

    private String composeTemplateXml() {
        return wrapTemplateXml(exampleInnerTemplate);
    }

    private File composeTemplateXmlToFile() throws IOException {
        return writeTemplateFromString(composeTemplateXml());
    }

    private String composeTemplateXml(Map<String, String> properties) {
        return wrapTemplateXml(addPropertiesToTemplate(exampleInnerTemplate, properties));
    }

    private File composeTemplateXmlToFile(Map<String, String> properties) throws IOException {
        return writeTemplateFromString(composeTemplateXml(properties));
    }

    @Test
    public void testRenderTemplateFileDoesNotModifyWithoutProperties() throws IOException {
        File file = composeTemplateXmlToFile();

        String result = tr.renderTemplateFile(file);

        assertThat(result).isEqualTo(composeTemplateXml());
    }

    @Test
    public void testRenderTemplateFileDoesNotModifyWithEmptyPropertiesFromInstantiate() throws IOException {
        TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties0);
        File file = composeTemplateXmlToFile();

        String result = trWithCustomProperties.renderTemplateFile(file);

        assertThat(result).isEqualTo(composeTemplateXml());
    }

    @Test
    public void testRenderTemplateFileDoesNotModifyWithEmptyPropertiesFromWithinTemplate() throws IOException {
        File file = composeTemplateXmlToFile(properties0);

        String result = tr.renderTemplateFile(file);

        assertThat(result).isEqualTo(composeTemplateXml(properties0));
    }

    @Test
    public void testRenderTemplateFileSubstitutedPropertiesFromInstantiate() throws IOException {
        TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties1);
        File file = composeTemplateXmlToFile();

        String result = trWithCustomProperties.renderTemplateFile(file);

        assertThat(result).doesNotContain("$" + property1key);
        assertThat(result).doesNotContain("${" + property1key + "}");
        assertThat(result).contains("$" + property2key);
        assertThat(result).contains("<test" + property1key + ">" + property1value + "</test" + property1key + ">");
    }

    @Test
    public void testRenderTemplateFileSubstitutedPropertiesFromWithinTemplate() throws IOException {
        File file = composeTemplateXmlToFile(properties1);

        String result = tr.renderTemplateFile(file);

        assertThat(result).doesNotContain("$" + property1key);
        assertThat(result).doesNotContain("${" + property1key + "}");
        assertThat(result).contains("$" + property2key);
        assertThat(result).contains("<test" + property1key + ">" + property1value + "</test" + property1key + ">");
    }

    @Test
    public void testRenderTemplateFileSubstitutedPropertiesFromBothDirections() throws IOException {
        TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties2);
        File file = composeTemplateXmlToFile(properties1);

        String result = trWithCustomProperties.renderTemplateFile(file);

        assertThat(result).doesNotContain("$" + property1key);
        assertThat(result).doesNotContain("${" + property1key + "}");
        assertThat(result).doesNotContain("$" + property2key);
        assertThat(result).contains("<test" + property1key + ">" + property1value + "</test" + property1key + ">");
        assertThat(result).contains("<test" + property2key + ">" + property2value + "</test" + property2key + ">");
    }

    @Test
    public void testRenderTemplateFileSubstitutedPropertiesFromBothDirectionsAndUnusedProperties() throws IOException {
        TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties2and3);
        File file = composeTemplateXmlToFile(properties1and3);

        String result = trWithCustomProperties.renderTemplateFile(file);

        assertThat(result).doesNotContain("$" + property1key);
        assertThat(result).doesNotContain("${" + property1key + "}");
        assertThat(result).doesNotContain("$" + property2key);
        assertThat(result).contains("<test" + property1key + ">" + property1value + "</test" + property1key + ">");
        assertThat(result).contains("<test" + property2key + ">" + property2value + "</test" + property2key + ">");
    }

    @Test
    public void testRenderTemplateFileSubstitutesPropertiesWithDotsFromContext() throws IOException {
        TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(new HashMap<>());
        File file = writeTemplateFromString(wrapTemplateXml(addPropertiesToTemplate(exampleDotTemplate, new HashMap<>())));

        HashMap<String, Object> contextMap = new HashMap<>();
        // In order to resolve ${property.subproperty}, velocity searches the context for an object called "property" with
        // a field "subproperty". The value of "subproperty" will then replace ${property.subproperty}.
        // This can be arbitrarily nested.
        contextMap.put("property", new Property());
        String result = trWithCustomProperties.renderTemplateFile(file, contextMap);

        assertThat(result).doesNotContain("$" + propertyWithDotKey);
        assertThat(result).doesNotContain("${" + propertyWithDotKey + "}");
        assertThat(result).doesNotContain("$" + propertyWithDotKey);
        assertThat(result).contains("<test" + propertyWithDotValue + ">" + propertyWithDotValue + "</test" + propertyWithDotValue + ">");
    }

    @Test(expected = ExecutionException.class)
    public void testRenderTemplateFileShouldNotReturnInvalidXmlIfTemplateWasInvalid() throws IOException {
        String invalidXmlTemplate = "<invalid xml";
        File file = writeTemplateFromString(invalidXmlTemplate);

        tr.renderTemplateFile(file);
    }

    // This class is needed to resolve properties with dots.
    public class Property {
        private final String subproperty = "propertyValue";

        public String getSubproperty() {
            return subproperty;
        }
    }

    @Test
    public void testRenderWorkflowFileWithSystemEnvironmentVariables() throws IOException, ParserConfigurationException, SAXException{
        environmentVariables.set("ANTENNATESTVARIABLE", "WARN");

        Map<String, String> systemEnvs = System.getenv();
        assertThat(systemEnvs.get("ANTENNATESTVARIABLE")).isEqualTo("WARN");

        HashMap<String, Object> contextMap = new HashMap<>();

        File workflowDefFile = new File(TemplateRendererTest.class.getResource("/workflow.xml").getFile());
        TemplateRenderer tr = new TemplateRenderer(contextMap);
        String renderedWorkflow = tr.renderTemplateFile(workflowDefFile);

        Workflow workflow;
        XmlSettingsReader workflowReader = new XmlSettingsReader(renderedWorkflow);
        workflow = workflowReader.getComplexType("workflow", Workflow.class);

        assertThat(workflow.getProcessors().getStep().stream().findFirst().get().getConfiguration().getEntry()
                .stream()
                .filter(entry -> entry.getKey().equals("incompleteSourcesSeverity"))
                .findFirst()
                .get()
                .getValue())
                .isEqualTo("WARN");
    }
}
