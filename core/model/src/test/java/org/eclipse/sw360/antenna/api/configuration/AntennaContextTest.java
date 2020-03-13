/*
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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AntennaContextTest {
    /**
     * Returns a new builder for a tool configuration with no special settings.
     *
     * @return the new builder
     */
    private static ToolConfiguration.ConfigurationBuilder defaultToolConfigurationBuilder() {
        return new ToolConfiguration.ConfigurationBuilder();
    }

    /**
     * Creates a new Antenna context from the given builders. The tool config
     * is created first from the corresponding builder and then passed to the
     * context builder.
     *
     * @param contextBuilder    the builder for the context
     * @param toolConfigBuilder the builder for the tool config
     * @return the resulting {@code AntennaContext}
     */
    private static AntennaContext createContext(AntennaContext.ContextBuilder contextBuilder,
                                                ToolConfiguration.ConfigurationBuilder toolConfigBuilder) {
        return contextBuilder.setToolConfiguration(toolConfigBuilder.buildConfiguration())
                .buildContext();
    }

    /**
     * Creates a new Antenna context from the given builder using a default
     * tool configuration.
     *
     * @param contextBuilder the builder for the context
     * @return the resulting {@code AntennaContext}
     */
    private static AntennaContext createContext(AntennaContext.ContextBuilder contextBuilder) {
        return createContext(contextBuilder, defaultToolConfigurationBuilder());
    }

    @Test
    public void testDefaultProperties() {
        IProject project = mock(IProject.class);
        Configuration configuration = mock(Configuration.class);
        ToolConfiguration toolConfiguration = defaultToolConfigurationBuilder().buildConfiguration();
        IProcessingReporter reporter = mock(IProcessingReporter.class);

        AntennaContext context = new AntennaContext.ContextBuilder()
                .setConfiguration(configuration)
                .setProcessingReporter(reporter)
                .setProject(project)
                .setToolConfiguration(toolConfiguration)
                .buildContext();
        assertThat(context.getConfiguration()).isEqualTo(configuration);
        assertThat(context.getProcessingReporter()).isEqualTo(reporter);
        assertThat(context.getProject()).isEqualTo(project);
        assertThat(context.getToolConfiguration()).isEqualTo(toolConfiguration);
        assertThat(context.getHttpClient()).isNotNull();
    }

    @Test
    public void testObjectMapper() {
        AntennaContext context = createContext(new AntennaContext.ContextBuilder());

        ObjectMapper mapper = context.getObjectMapper();
        assertThat(mapper).isSameAs(ServiceFactory.getObjectMapper());
    }

    @Test
    public void testHttpClient() {
        final String proxyHost = "test.proxy";
        final int proxyPort = 5555;
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(serviceFactory.createHttpClient(true, proxyHost, proxyPort)).thenReturn(httpClient);
        ToolConfiguration.ConfigurationBuilder configurationBuilder = defaultToolConfigurationBuilder()
                .setProxyHost(proxyHost)
                .setProxyPort(proxyPort);

        AntennaContext context =
                createContext(new AntennaContext.ContextBuilder(serviceFactory), configurationBuilder);
        assertThat(context.getHttpClient()).isEqualTo(httpClient);
    }

    @Test
    public void testContextExtensionEmptyNoMatch() {
        AntennaContext context = createContext(new AntennaContext.ContextBuilder());

        assertThat(context.getGeneric(Object.class)).isEmpty();
    }

    @Test
    public void testContextExtensionNoMatch() {
        ContextExtension extension = new ContextExtension();
        assertThat(extension.put(this)).isTrue();
        AntennaContext context =
                createContext(new AntennaContext.ContextBuilder().setContextExtensions(extension));

        assertThat(context.getGeneric(JsonBean.class)).isEmpty();
    }

    @Test
    public void testContextExtensionDirectMatch() {
        ContextExtension extension = new ContextExtension();
        JsonBean bean = new JsonBean();
        extension.put(bean);
        AntennaContext context =
                createContext(new AntennaContext.ContextBuilder().setContextExtensions(extension));

        assertThat(context.getGeneric(JsonBean.class)).contains(bean);
    }

    @Test
    public void testContextExtensionBaseClassMatch() {
        Runnable runnable = () -> {
        };
        ContextExtension extension = new ContextExtension();
        extension.put(runnable);
        AntennaContext context =
                createContext(new AntennaContext.ContextBuilder().setContextExtensions(extension));

        assertThat(context.getGeneric(Runnable.class)).contains(runnable);
        assertThat(context.getGeneric(Object.class)).contains(runnable);
    }
}
