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

package org.eclipse.sw360.antenna.frontend.mojo;

import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
import org.eclipse.sw360.antenna.api.FrontendCommons;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.core.AntennaCore;
import org.eclipse.sw360.antenna.frontend.AntennaFrontend;
import org.eclipse.sw360.antenna.frontend.AntennaFrontendHelper;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.util.TemplateRenderer;
import org.eclipse.sw360.antenna.workflow.WorkflowFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides configuration and execution services for using Antenna as a Maven Mojo.
 */

public abstract class AbstractAntennaMojoFrontend extends AbstractMojo implements AntennaFrontend {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAntennaMojoFrontend.class);
    private final Map<String, IAttachable> output = new HashMap<>();

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    @Component
    private LegacySupport mvnBuildContext;

    @Component
    private ArtifactResolver mvnArtifactResolver;

    @Component
    private RepositorySystem mvnRepositorySystem;

    @Component
    private MavenProjectHelper attachHelper;

    @Parameter(property = "antennaTargetDirectory",
            defaultValue = "${project.build.directory}" + "${file.separator}" + FrontendCommons.ANTENNA_DIR)
    private String antennaTargetDirectory;

    @Parameter(property = "configFiles")
    private List<File> configFiles;

    @Parameter(property = "configFileUri")
    private List<URI> configFileUris;

    @Parameter(property = "clmScanResultPath", defaultValue = "${project.build.outputDirectory}/clm-scan-result.json")
    private String clmScanResultPath;

    @Parameter(property = "reportDataUrl")
    private String reportDataUrl;

    @Parameter(property = "scanDir")
    private String scanDir;

    @Parameter(property = "disableP2Resolving", defaultValue = "true")
    private boolean disableP2Resolving;

    @Parameter(property = "attachAll", defaultValue = "false")
    private boolean attachAll;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    // parameter for disclosure document
    @Parameter(property = "fullname", defaultValue = "${project.name}")
    private String productFullname;

    @Parameter(property = "version", defaultValue = "1.0")
    private String version;

    @Parameter(property = "productName", defaultValue = "${project.artifactId}")
    private String productName;

    @Parameter(property = "companyName")
    private String companyName;

    @Parameter(property = "copyrightHoldersName")
    private String copyrightHoldersName;

    @Parameter(property = "copyrightNotice")
    private String copyrightNotice;

    @Parameter(property = "disclosureDocumentNotes")
    private String disclosureDocumentNotes;

    @Parameter(property = "disclosureTemplatePath")
    private String disclosureDocTemplatePath;

    @Parameter(property = "filesToAttach")
    private List<String> filesToAttach;

    @Parameter(property = "sourcesRepositoryUrl")
    private String sourcesRepositoryUrl;

    @Parameter(property = "workflowDefinitionFile")
    private String workflowDefinitionFile;

    @Parameter(property = "workflow")
    private Workflow workflow;

    @Parameter(property = "showCopyrightStatements", defaultValue = "false")
    private boolean showCopyrightStatements;

    @Parameter(property = "encodingCharSet", defaultValue = "UTF-8")
    private String encodingCharSet;

    @Parameter(property = "proxyHost", defaultValue = "")
    private String proxyHost;

    @Parameter(property = "proxyPort", defaultValue = "0")
    private int proxyPort;

    @Parameter(property = "proxyId", defaultValue = "")
    private String proxyId;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    /**
     * The dependency tree builder to use.
     */
    @Component( hint = "default", role = DependencyGraphBuilder.class)
    private DependencyGraphBuilder dependencyGraphBuilder;

    private void getProxySettingsFromSettings() {
        boolean noProxyIsYetConfigured = proxyHost == null || "".equals(proxyHost) || proxyPort <= 0;
        Predicate<Proxy> proxyContainsUnsupportedConfiguration = proxy ->
                (proxy.getUsername() != null && ! "".equals(proxy.getUsername())) ||
                        (proxy.getPassword() != null && ! "".equals(proxy.getPassword())) ||
                        (proxy.getNonProxyHosts() != null && ! "".equals(proxy.getNonProxyHosts()));

        if (noProxyIsYetConfigured) {
            settings.getProxies().stream()
                    .filter(Proxy::isActive)
                    .peek(proxy -> {
                        if(!"http".equals(proxy.getProtocol())){
                            LOGGER.info("Maven settings contain proxy with unsupportet protocol=[" + proxy.getProtocol() + "]");
                        }
                    })
                    .filter(proxy -> "http".equals(proxy.getProtocol()))
                    .filter(proxy -> "".equals(proxyId) || proxy.getId().equals(proxyId))
                    .peek(proxy -> {
                        if (proxyContainsUnsupportedConfiguration.test(proxy)) {
                            LOGGER.info("Maven settings contain proxy configuration which can not be handled by antenna completely (e.g. authentication, nonProxyHosts)");
                        }
                    })
                    .forEach(proxy -> {
                        if (proxy.getHost() != null && !"".equals(proxy.getHost()) && proxy.getPort() > 0) {
                            LOGGER.info("Use proxy configuration with id=[" + proxy.getId() + "] from Maven settings");
                            proxyHost = proxy.getHost();
                            proxyPort = proxy.getPort();
                        }
                    });
        }
    }

    /**
     * Start point of the Antenna maven-plugin.
     */
    @SuppressWarnings("ReturnInsideFinallyBlock")
    @Override
    public void execute() throws MojoExecutionException {
        AntennaContext context;
        AntennaCore antennaCore;
        try{
            final AntennaFrontendHelper antennaFrontendHelper = init();
            context = antennaFrontendHelper.buildAntennaContext();
            antennaCore = antennaFrontendHelper.buildAntennaCore(context);
        } catch (AntennaConfigurationException e) {
            LOGGER.error("AntennaCore was not initialized sucessfully");
            throw new MojoExecutionException("Exception during Antenna initialization", e);
        }

        try {
            output.putAll(antennaCore.compose());
        } catch (AntennaExecutionException | AntennaException e) {
            LOGGER.error("Antenna execution failed due to:", e);
            throw new MojoExecutionException("Exception during Antenna execution", e);
        } finally {
            IAttachable report = antennaCore.writeAnalysisReport();
            output.put(IProcessingReporter.getIdentifier(), report);

            LOGGER.info("Attaching artifacts to project");
            ArtifactAttacher attacher = new ArtifactAttacher(context);
            attacher.attach(output);
            LOGGER.info("Attaching artifacts to project..done");
        }
    }

    @Override
    public AntennaFrontendHelper init() {
        getProxySettingsFromSettings();
        ToolConfiguration toolConfiguration = loadConfiguration();
        WrappedMavenProject wrappedMavenProject = new WrappedMavenProject(mvnBuildContext.getSession().getCurrentProject());

        WrappedDependencyNodes wrappedDependencyNodes = getWrappedDependencyNodes();

        return new AntennaFrontendHelper(wrappedMavenProject)
                .setToolConfiguration(toolConfiguration)
                .putGeneric(attachHelper)
                .putGeneric(mvnArtifactResolver)
                .putGeneric(project)
                .putGeneric(settings)
                .putGeneric(mvnBuildContext)
                .putGeneric(mvnRepositorySystem)
                .putGeneric(wrappedDependencyNodes);
    }

    /**
     * Put parameters into context ready for a Antenna execution.
     */
    private ToolConfiguration loadConfiguration() throws AntennaExecutionException {
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("project", project);
        contextMap.put("basedir", project.getBasedir().toPath().toString());

        // propagate proxy related properties
        System.getProperties()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().toString().startsWith("proxy"))
                .forEach(e -> contextMap.put(e.getKey().toString(), e.getValue().toString()));

        TemplateRenderer tr = new TemplateRenderer(contextMap);

        Optional<File> workflowDefFile = Optional.ofNullable(workflowDefinitionFile)
                .map(File::new);

        Workflow finalWorkflow;
        try {
            finalWorkflow = WorkflowFileLoader.loadWorkflowFromClassPath(workflowDefFile, tr);
            WorkflowFileLoader.overrideWorkflow(finalWorkflow, workflow);
        } catch (AntennaConfigurationException e) {
            throw new AntennaExecutionException("Failed to load workflow", e);
        }

        ToolConfiguration.ConfigurationBuilder toolConfigBuilder = new ToolConfiguration.ConfigurationBuilder()
                .setAntennaTargetDirectory(antennaTargetDirectory).setAttachAll(attachAll)
                .setFilesToAttach(filesToAttach)
                .setConfigFiles(configFiles).setConfigFileUris(configFileUris)
                .setProductName(productName).setProductFullName(productFullname)
                .setVersion(version).setScanDir(scanDir)
                .setSkipAntennaExecution(skip)
                .setMavenInstalled(true)  // when using the maven plugin, maven is surely available
                .setCopyrightHoldersName(copyrightHoldersName).setCopyrightNotice(copyrightNotice)
                .setDisclosureDocumentNotes(disclosureDocumentNotes).setWorkflow(finalWorkflow)
                .setProxyHost(proxyHost).setProxyPort(proxyPort)
                .setShowCopyrightStatements(showCopyrightStatements).setEncoding(encodingCharSet);

        return toolConfigBuilder.buildConfiguration();
    }

    /**
     * @return List of all dependency nodes in the maven dependency graph.
     */
    private WrappedDependencyNodes getWrappedDependencyNodes() {
        WrappedDependencyNodes wrappedDependencyNodes = null;

        if(dependencyGraphBuilder != null) {
            try {
                MavenSession session = mvnBuildContext.getSession();
                ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
                buildingRequest.setProject(session.getCurrentProject());

                DependencyNode dependencyNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, null);
                CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
                dependencyNode.accept(visitor);
                wrappedDependencyNodes = new WrappedDependencyNodes(visitor.getNodes());
            } catch (DependencyGraphBuilderException e) {
                LOGGER.error(String.format("Could not build maven dependency tree: %s", e.getMessage()));
            }
        }
        return wrappedDependencyNodes;
    }

    public Map<String, IAttachable> getOutputs() {
        return output;
    }

    public void setWorkflow(InlineWorkflowParsingResult workflowParsingResult) {
        warnIfWorkflowIsPotentiallyWrong(workflowParsingResult);

        workflow = workflowParsingResult;
    }

    private void warnIfWorkflowIsPotentiallyWrong(Workflow workflowParsingResult) {
        boolean isPotentiallyWrong = Stream.of(workflowParsingResult.getAnalyzers().getStep(),
                workflowParsingResult.getGenerators().getStep(),
                workflowParsingResult.getProcessors().getStep(),
                workflowParsingResult.getOutputHandlers().getStep())
                .flatMap(List::stream)
                .flatMap(workflowStep -> workflowStep.getConfiguration().getEntry().stream())
                .anyMatch(entry -> entry.getKey() == null && entry.getEntryKey() == null ||
                        entry.getValue() == null && entry.getEntryValue() == null);

        if (isPotentiallyWrong) {
            String msg = "The parsed workflow in the pom contains workflow configuration entries without key or without value.\n" +
                    "This is a strong hint that the configuration uses the wrong format:\n" +
                    "\tinstead of the short format with attributes, i.e.\n\t\t<entry key=\"somKey\" value=\"someValue\"/>\n" +
                    "\tone has to use the alternative format:\n\t\t<entry><entryKey>someKey</entryKey><entryValue>someValue</entryValue></entry>";
            LOGGER.warn(msg);
        }
    }
}
