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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateRendererTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private String property1key = "property1";
	private String property1value = "property1value";
	private String property2key = "property2";
	private String property2value = "property2value";
	private String property3key = "property3";
	private String property3value = "property3value";
	private TemplateRenderer tr;
	private Map<String,String> properties0 = new HashMap<>();
	private Map<String,String> properties1 = new HashMap<>();
	private Map<String,String> properties2 = new HashMap<>();
	private Map<String,String> properties1and2 = new HashMap<>();
	private Map<String,String> properties1and3 = new HashMap<>();
	private Map<String,String> properties2and3 = new HashMap<>();

	private String exampleInnerTemplate = "<tests>\n"
			+ "<test"+property1key+">$"+property1key+"</test"+property1key+">\n"
			+ "<test"+property1key+">${"+property1key+"}</test"+property1key+">\n"
			+ "<test"+property2key+">$"+property2key+"</test"+property2key+">\n"
			+ "</tests>";

	@Before
	public void before() {
		tr = new TemplateRenderer();

		properties1.put(property1key,property1value);
		properties2.put(property2key,property2value);
		properties1and2.put(property1key,property1value);
		properties1and2.put(property2key,property2value);
		properties1and3.put(property1key,property1value);
		properties1and3.put(property3key,property3value);
		properties2and3.put(property2key,property2value);
		properties2and3.put(property3key,property3value);
	}

	private TemplateRenderer mkTemplateRendererWithPropertiesMap(Map<String,String> properties) {
		return new TemplateRenderer(properties.entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getKey(),
						e -> (Object) e.getValue())));
	}

	private String addPropertiesToTemplate(String innerTemplate, Map<String,String> properties) {
	    String propertiesXml = properties.entrySet().stream()
				.map(e -> "<"+e.getKey()+">" +e.getValue()+ "</"+e.getKey()+">")
                .reduce("", (s1,s2) -> s1+"\n"+s2);
	    return "<properties>\n" + propertiesXml + "\n</properties>\n" + innerTemplate;
	}

	private String wrapTemplateXml(String innerTemplate) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>\n" + innerTemplate + "\n</project>";
	}

	private File writeTemplateFromString(String template) throws IOException {
		final File templateFile = folder.newFile("template.xml");
		FileUtils.writeStringToFile(templateFile, template);
		return templateFile;
	}

	private String composeTemplateXml() throws IOException {
	    return wrapTemplateXml(exampleInnerTemplate);
	}

	private File composeTemplateXmlToFile() throws IOException {
		return writeTemplateFromString(composeTemplateXml());
	}

	private String composeTemplateXml(Map<String,String> properties) throws IOException {
		return wrapTemplateXml(addPropertiesToTemplate(exampleInnerTemplate, properties));
	}

	private File composeTemplateXmlToFile(Map<String,String> properties) throws IOException {
		return writeTemplateFromString(composeTemplateXml(properties));
	}

	@Test
	public void testRenderTemplateFileDoesNotModifyWithoutProperties() throws IOException {
		File file = composeTemplateXmlToFile();

		String result = tr.renderTemplateFile(file);

		assertThat(result).isEqualTo(composeTemplateXml());
	}

	@Test
	public void testRenderTemplateFileDoesNotModifyWithEmptyPropertiesFromInstanciate() throws IOException {
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
	public void testRenderTemplateFileSubstitutedPropertiesFromInstanciate() throws IOException {
		TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties1);
		File file = composeTemplateXmlToFile();

		String result = trWithCustomProperties.renderTemplateFile(file);

		assertThat(result).doesNotContain("$"+property1key);
		assertThat(result).doesNotContain("${"+property1key+"}");
		assertThat(result).contains("$" + property2key);
		assertThat(result).contains("<test"+property1key+">"+property1value+"</test"+property1key+">");
	}

	@Test
	public void testRenderTemplateFileSubstitutedPropertiesFromWithinTemplate() throws IOException {
	    File file = composeTemplateXmlToFile(properties1);

	    String result = tr.renderTemplateFile(file);
	    
	    assertThat(result).doesNotContain("$"+property1key);
	    assertThat(result).doesNotContain("${"+property1key+"}");
		assertThat(result).contains("$"+property2key);
	    assertThat(result).contains("<test"+property1key+">"+property1value+"</test"+property1key+">");
	}

	@Test
	public void testRenderTemplateFileSubstitutedPropertiesFromBothDirections() throws IOException {
		TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties2);
		File file = composeTemplateXmlToFile(properties1);

		String result = trWithCustomProperties.renderTemplateFile(file);

		assertThat(result).doesNotContain("$"+property1key);
		assertThat(result).doesNotContain("${"+property1key+"}");
		assertThat(result).doesNotContain("$"+property2key);
		assertThat(result).contains("<test"+property1key+">"+property1value+"</test"+property1key+">");
		assertThat(result).contains("<test"+property2key+">"+property2value+"</test"+property2key+">");
	}

	@Test
	public void testRenderTemplateFileSubstitutedPropertiesFromBothDirectionsAndUnusedProperties() throws IOException {
		TemplateRenderer trWithCustomProperties = mkTemplateRendererWithPropertiesMap(properties2and3);
		File file = composeTemplateXmlToFile(properties1and3);

		String result = trWithCustomProperties.renderTemplateFile(file);

		assertThat(result).doesNotContain("$"+property1key);
		assertThat(result).doesNotContain("${"+property1key+"}");
		assertThat(result).doesNotContain("$"+property2key);
		assertThat(result).contains("<test"+property1key+">"+property1value+"</test"+property1key+">");
		assertThat(result).contains("<test"+property2key+">"+property2value+"</test"+property2key+">");
	}

	@Test(expected = Exception.class)
	public void testRenderTemplateFileShouldNotReturnInvalidXmlIfTemplateWasInvalid() throws IOException {
	    String invalidXmlTemplate = "<invalid xml";
		File file = writeTemplateFromString(invalidXmlTemplate);

		String result = tr.renderTemplateFile(file);

		assertThat(result).isNotEqualTo(invalidXmlTemplate);
	}
}
