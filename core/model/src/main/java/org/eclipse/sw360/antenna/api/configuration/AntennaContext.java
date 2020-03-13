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

package org.eclipse.sw360.antenna.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.service.ServiceFactory;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.model.Configuration;

import java.util.Optional;

/**
 * <p>
 * A context class that maintains state regarding the current execution of
 * Antenna.
 * </p>
 * <p>
 * An instance of this class is available to all components taking part in an
 * Antenna run. It allows access to central information, to the different
 * configuration sources, and to useful service objects with a global scope.
 * </p>
 */
public class AntennaContext {

    private boolean debug;

    private Configuration configuration;
    private final ToolConfiguration toolConfiguration;

    private final IProject project;
    private IProcessingReporter processingReporter;

    private final ContextExtension contextExtension;

    private final ServiceFactory serviceFactory;

    private AntennaContext(ContextBuilder builder) {
        this.configuration = builder.configuration;
        this.toolConfiguration = builder.toolConfiguration;

        this.project = builder.project;

        this.contextExtension = builder.contextExtension;

        this.processingReporter = builder.processingReporter;

        this.serviceFactory = builder.serviceFactory;
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

    public <T> Optional<T> getGeneric(Class<? extends T> clazz){
        return this.contextExtension.get(clazz);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug() {
        return debug;
    }

    /**
     * Returns a configured {@code HttpClient} instance that can be used for
     * all interactions with HTTP clients. The HTTP client is created when it
     * is accessed for the first time and initialized from the current tool
     * configuration. It is a thread-safe object and can thus be shared by
     * all components that need to send HTTP requests.
     *
     * @return the shared HTTP client instance
     */
    public HttpClient getHttpClient() {
        return serviceFactory.createHttpClient(toolConfiguration.useProxy(),
                toolConfiguration.getProxyHost(), toolConfiguration.getProxyPort());
    }

    /**
     * Returns a configured {@code ObjectMapper} for JSON serialization. The
     * object is created and initialized when it is accessed for the first
     * time. As it is thread-safe, it can (and should) be used by all
     * components that need to convert objects to and from JSON.
     *
     * @return the shared JSON object mapper
     */
    public ObjectMapper getObjectMapper() {
        return ServiceFactory.getObjectMapper();
    }

    public static class ContextBuilder {
        /**
         * The factory for creating the shared services.
         */
        private final ServiceFactory serviceFactory;

        private Configuration configuration;
        private ToolConfiguration toolConfiguration;
        private IProject project;
        private ContextExtension contextExtension = new ContextExtension();
        private IProcessingReporter processingReporter;

        public ContextBuilder() {
            this(new ServiceFactory());
        }

        /**
         * Creates a new instance of {@code ContextBuilder} and sets the
         * {@code ServiceFactory}. This constructor is used for testing
         * purposes.
         *
         * @param serviceFactory the {@code ServiceFactory}
         */
        ContextBuilder(ServiceFactory serviceFactory) {
            this.serviceFactory = serviceFactory;
        }

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
