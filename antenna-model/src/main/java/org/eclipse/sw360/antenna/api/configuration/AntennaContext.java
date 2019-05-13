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

package org.eclipse.sw360.antenna.api.configuration;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.model.Configuration;

import java.util.Optional;

/**
 * Maintains state regarding the current execution of Antenna.
 */
public class AntennaContext {

    private boolean debug;

    private Configuration configuration;
    private final ToolConfiguration toolConfiguration;

    private final IProject project;
    private IProcessingReporter processingReporter;

    private final ContextExtension contextExtension;

    private AntennaContext(ContextBuilder builder) {
        this.configuration = builder.configuration;
        this.toolConfiguration = builder.toolConfiguration;

        this.project = builder.project;

        this.contextExtension = builder.contextExtension;

        this.processingReporter = builder.processingReporter;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public ToolConfiguration getToolConfiguration() {
        return this.toolConfiguration;
    }

    public IProject getProject() {
        return this.project;
    }

    public IProcessingReporter getProcessingReporter() {
        return this.processingReporter;
    }

    public <T> Optional<T> getGeneric(Class clazz){
        return this.contextExtension.get(clazz);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug(){
        return debug;
    }

    public static class ContextBuilder {
        private Configuration configuration;
        private ToolConfiguration toolConfiguration;
        private IProject project;
        private ContextExtension contextExtension = new ContextExtension();
        private IProcessingReporter processingReporter;

        public ContextBuilder setToolConfiguration(ToolConfiguration configuration) {
            this.toolConfiguration = configuration;
            return this;
        }

        public ContextBuilder setProject(IProject project) {
            this.project = project;
            return this;
        }

        public ContextBuilder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public ContextBuilder setProcessingReporter(IProcessingReporter processingReporter) {
            this.processingReporter = processingReporter;
            return this;
        }

        public AntennaContext buildContext() {
            return new AntennaContext(this);
        }

        public ContextBuilder setContextExtensions(ContextExtension contextExtension) {
            this.contextExtension = contextExtension;
            return this;
        }
    }
}