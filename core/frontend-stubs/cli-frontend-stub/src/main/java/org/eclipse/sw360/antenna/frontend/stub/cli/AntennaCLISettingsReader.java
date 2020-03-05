/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.stub.cli;

import org.eclipse.sw360.antenna.api.FrontendCommons;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.frontend.MetaDataStoringProject;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.util.TemplateRenderer;
import org.eclipse.sw360.antenna.util.XmlSettingsReader;
import org.eclipse.sw360.antenna.workflow.WorkflowFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AntennaCLISettingsReader {

    private static final Logger log = LoggerFactory.getLogger(AntennaCLISettingsReader.class);
    private String antennaConfXpath;

    private TemplateRenderer tr = new TemplateRenderer();

    public AntennaCLISettingsReader() {
        this("antenna-maven-plugin");
    }

    public AntennaCLISettingsReader(String pluginDescendantArtifactName) {
        antennaConfXpath = "descendant::plugin[artifactId='" + pluginDescendantArtifactName + "']/descendant-or-self::configuration";
    }

    private void readProjectStringSetting(XmlSettingsReader reader, String name, Consumer<String> setter) {
        String value = reader.getStringProperty(name);
        if (value != null) {
            setter.accept(value);
        }
    }

    private void readAntennaStringSetting(XmlSettingsReader reader, String name, Consumer<String> setter) {
        String value = reader.getStringPropertyByXPath(antennaConfXpath, name);
        if (value != null) {
            setter.accept(value);
        }
    }

    private void readAntennaStringSetting(XmlSettingsReader reader, String name, String defaultValue, Consumer<String> setter) {
        String value = reader.getStringPropertyByXPath(antennaConfXpath, name, defaultValue);
        if (value != null) {
            setter.accept(value);
        }
    }

    private void readAntennaIntSetting(XmlSettingsReader reader, String name, int defaultValue, Consumer<Integer> setter) {
        int value = reader.getIntProperty(antennaConfXpath, name, defaultValue);
        setter.accept(value);
    }

    private void readAntennaBooleanSetting(XmlSettingsReader reader, String name, boolean defaultValue, Consumer<Boolean> setter) {
        boolean value = reader.getBooleanProperty(antennaConfXpath, name, defaultValue);
        setter.accept(value);
    }

    private void readStringListSetting(XmlSettingsReader reader, String name, Consumer<List<String>> setter) {
        List<String> value = reader.getStringListProperty(name);
        if (value != null) {
            setter.accept(value);
        }
    }

    private List<File> buildFileList(List<String> paths) {
        return paths.stream().map(File::new).collect(Collectors.toList());
    }

    private void readFileListSetting(XmlSettingsReader reader, String name, Consumer<List<File>> setter) {
        readStringListSetting(reader, name, v -> setter.accept(buildFileList(v)));
    }

    public ToolConfiguration readSettingsToToolConfiguration(MetaDataStoringProject project) {
        setVersionFromPom(project);
        XmlSettingsReader reader = getSettingsReader(project);
        return readSettingsToToolConfiguration(reader, project);
    }

    private void setVersionFromPom(MetaDataStoringProject project) {
        File pomFile = project.getConfigFile();
        try {
            String pom = new String(Files.readAllBytes(pomFile.toPath()), StandardCharsets.UTF_8);
            XmlSettingsReader reader = new XmlSettingsReader(pom);
            String version = reader.getStringPropertyByXPath("project", "version");
            if (version != null && !"".equals(version)) {
                project.setVersion(version);
                return;
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.warn("Failed to parse the pom to extract the version", e);
        }
        if (project.getVersion() == null || project.getVersion().isEmpty()) {
            project.setVersion("1.0");
        }
    }

    private XmlSettingsReader getSettingsReader(MetaDataStoringProject project) {
        File pomFile = project.getConfigFile();

        // Since we are not Maven we must render the pom.xml first
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("project", project);
        contextMap.put("basedir", project.getBasedir());

        Optional.ofNullable(project.getPropertiesFile())
                .map(this::mapProperties)
                .ifPresent(contextMap::putAll);

        System.getProperties()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().toString().startsWith("proxy"))
                .forEach(e -> contextMap.put(e.getKey().toString(), e.getValue().toString()));

        String renderedPom = tr.renderTemplateFile(pomFile, contextMap);

        try {
            return new XmlSettingsReader(renderedPom);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ConfigurationException("Problem parsing the config: " + e.getMessage());
        }
    }

    private String getDefaultAntennaTargetDirectory(MetaDataStoringProject project) {
        Path buildDir = Paths.get(project.getBuildDirectory());
        return buildDir.resolve(FrontendCommons.ANTENNA_DIR).toString();
    }

    ToolConfiguration.ConfigurationBuilder readBasicSettingsToToolConfigurationBuilder(XmlSettingsReader reader, MetaDataStoringProject project)
            throws IllegalArgumentException {
        ToolConfiguration.ConfigurationBuilder toolConfigBuilder = new ToolConfiguration.ConfigurationBuilder();

        // Simple strings
        readProjectStringSetting(reader, "artifactId", project::setProjectId);
        readAntennaStringSetting(reader, "antennaTargetDirectory", getDefaultAntennaTargetDirectory(project), toolConfigBuilder::setAntennaTargetDirectory);
        readAntennaStringSetting(reader, "scanDir", toolConfigBuilder::setScanDir);
        readAntennaStringSetting(reader, "productName", reader.getStringProperty("name"), toolConfigBuilder::setProductName);
        readAntennaStringSetting(reader, "productFullname", reader.getStringProperty("name"), toolConfigBuilder::setProductFullName);
        readAntennaStringSetting(reader, "version", "1.0", toolConfigBuilder::setVersion);
        readAntennaStringSetting(reader, "companyName", toolConfigBuilder::setCompanyName);
        readAntennaStringSetting(reader, "copyrightHoldersName", toolConfigBuilder::setCopyrightHoldersName);
        readAntennaStringSetting(reader, "copyrightNotice", toolConfigBuilder::setCopyrightNotice);
        readAntennaStringSetting(reader, "attributionDocumentNotes", toolConfigBuilder::setAttributionDocumentNotes);
        readAntennaStringSetting(reader, "encodingCharSet", toolConfigBuilder::setEncoding);
        readAntennaStringSetting(reader, "proxyHost", toolConfigBuilder::setProxyHost);

        readAntennaIntSetting(reader, "proxyPort", 0, toolConfigBuilder::setProxyPort);

        // Booleans
        readAntennaBooleanSetting(reader, "attachAll", false, toolConfigBuilder::setAttachAll);
        readAntennaBooleanSetting(reader, "skip", false, toolConfigBuilder::setSkipAntennaExecution);
        readAntennaBooleanSetting(reader, "showCopyrightStatements", false, toolConfigBuilder::setShowCopyrightStatements);
        readAntennaBooleanSetting(reader, "isMavenInstalled", false, toolConfigBuilder::setMavenInstalled);

        // Other lists
        readStringListSetting(reader, "filesToAttach", toolConfigBuilder::setFilesToAttach);
        readFileListSetting(reader, "configFiles", toolConfigBuilder::setConfigFiles);

        return toolConfigBuilder;
    }

    /**
     * Extract properties from the specified pom.xml
     */
    public ToolConfiguration readSettingsToToolConfiguration(XmlSettingsReader reader, MetaDataStoringProject project) {
        ToolConfiguration.ConfigurationBuilder toolConfigBuilder = readBasicSettingsToToolConfigurationBuilder(reader, project);

        Optional<File> workflowDefFile = Optional.ofNullable(reader.getFileProperty("workflowDefinitionFile"));

        Workflow finalWorkflow = WorkflowFileLoader.loadWorkflowFromClassPath(workflowDefFile, tr);

        Workflow workflowFromConfig = reader.getComplexType("workflow", Workflow.class);
        if (workflowFromConfig != null) {
            WorkflowFileLoader.overrideWorkflow(finalWorkflow, workflowFromConfig);
        }

        toolConfigBuilder.setWorkflow(finalWorkflow);

        return toolConfigBuilder.buildConfiguration();
    }

    private Map<String, Object> mapProperties(File propertiesFile) {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            Properties prop = new Properties();

            prop.load(input);

            return prop.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            p -> p.getKey().toString(),
                            p -> p.getValue().toString()
                    ));
        } catch (IOException e) {
            throw new ConfigurationException("IO exception when reading properties file: " + e.getMessage());
        }
    }
}
