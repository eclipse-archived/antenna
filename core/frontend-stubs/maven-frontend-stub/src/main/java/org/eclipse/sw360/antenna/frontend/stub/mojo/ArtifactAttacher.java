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

package org.eclipse.sw360.antenna.frontend.stub.mojo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Attaches artifacts to the project in which the plugin is executed.
 */
public class ArtifactAttacher {

    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactAttacher.class);
    private IProcessingReporter reporter;
    private MavenProjectHelper projectHelper;
    private MavenProject currentProject;
    private boolean attachAll;
    private final ToolConfiguration toolConf;

    private boolean isSupported = true;

    /**
     * Attaches artifacts to the project in which the plugin is executed.
     *
     *            All artifacts which are created during the process of Antenna
     *            will be attached it this parameter is true
     */
    public ArtifactAttacher(AntennaContext context) {
        toolConf = context.getToolConfiguration();
        // TODO: only works in maven
        if(context.getProject().getRawProject() instanceof MavenProject){
            this.reporter = context.getProcessingReporter();
            this.projectHelper = (MavenProjectHelper) context.getGeneric(MavenProjectHelper.class)
                    .orElseThrow(() -> new AntennaExecutionException("MavenProjectHelper is not supported "));
            this.currentProject = (MavenProject) context.getProject().getRawProject();
            this.attachAll = toolConf.isAttachAll();
        }else{
            isSupported = false;
        }
    }

    /**
     * Attaches artifacts defined in the list filesToAttach.
     * (identifier,type,classifier)
     *
     */
    public void attach(Map<String, IAttachable> artifactsToAttach) {
        if(! isSupported) {
            LOGGER.warn("Tried to attach artifacts, but artifact attaching is not supported");
            return;
        }

        Predicate<Map.Entry<String, IAttachable>> filterFunc;
        if (attachAll) {
            filterFunc = key -> true;
        } else {
            Map<String, String[]> fileMap;
            fileMap = createFilesToAttach(toolConf.getFilesToAttach());
            filterFunc = key -> fileMap.containsKey(key.getKey());
        }
        artifactsToAttach.entrySet().stream()
                .filter(filterFunc)
                .forEach(a -> attachArtifact(a.getValue()));
    }

    private void attachArtifact(IAttachable attachable) {
        LOGGER.info("Attaching artifact {}", attachable.getFile());
        try {
            projectHelper.attachArtifact(currentProject, attachable.getType(), attachable.getClassifier(),
                    attachable.getFile());
        } catch (Exception e) {
            ProcessingMessage message = new ProcessingMessage(MessageType.ATTACHING_FAILURE,
                    attachable.getClassifier(),
                    String.format("The artifact %s couldn't be attached to the project: %s",
                            attachable.getClassifier(), e.getMessage()));
            this.reporter.add(message);
        }
    }

    private Map<String, String[]> createFilesToAttach(List<String> filesToAttach) {
        Map<String, String[]> fileInformation = new HashMap<>();
        for (String string : filesToAttach) {
            String[] split = string.split(",");
            if(split.length == 3){
                fileInformation.put(split[0], new String[] { split[1], split[2] });
            }
            else{
                LOGGER.warn("Wrong input were supplied to filesToAttach [{}]", string);
            }
        }
        return fileInformation;
    }
}
