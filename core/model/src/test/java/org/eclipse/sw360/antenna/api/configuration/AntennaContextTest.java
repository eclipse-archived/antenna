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
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.http.HttpClientFactory;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.eclipse.sw360.antenna.model.Configuration;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    /**
     * Tests the JSON object mapper. By reading a test JSON file, it is checked
     * whether the mapper has been correctly initialized; especially that
     * unknown properties are ignored.
     */
    @Test
    public void testObjectMapper() throws IOException {
        JsonBean expBean = new JsonBean();
        expBean.setName("test");
        expBean.setAge(42);
        AntennaContext context = createContext(new AntennaContext.ContextBuilder());

        ObjectMapper mapper = context.getObjectMapper();
        assertThat(mapper).isNotNull();
        JsonBean bean = mapper.readValue(getClass().getResource("/testPerson.json"), JsonBean.class);
        assertThat(bean).isEqualTo(expBean);
    }

    /**
     * Obtains the configuration that was passed to the given factory.
     *
     * @param factory the HTTP client factory
     * @return the configuration used to create a client
     */
    private static HttpClientConfig fetchHttpClientConfig(HttpClientFactory factory) {
        ArgumentCaptor<HttpClientConfig> captor = ArgumentCaptor.forClass(HttpClientConfig.class);
        verify(factory).newHttpClient(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testHttpClient() {
        HttpClientFactory factory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(factory.newHttpClient(any())).thenReturn(httpClient);

        AntennaContext context = createContext(new AntennaContext.ContextBuilder(factory));
        HttpClientConfig config = fetchHttpClientConfig(factory);
        assertThat(config.customObjectMapper()).contains(context.getObjectMapper());
        assertThat(config.proxySettings()).isEqualTo(ProxySettings.noProxy());
        assertThat(context.getHttpClient()).isEqualTo(httpClient);
    }

    @Test
    public void testHttpClientWithProxyConfiguration() {
        final String proxyHost = "test.proxy.net";
        final int proxyPort = 4242;
        HttpClientFactory factory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(factory.newHttpClient(any())).thenReturn(httpClient);
        ToolConfiguration.ConfigurationBuilder toolConfigBuilder = defaultToolConfigurationBuilder()
                .setProxyHost(proxyHost)
                .setProxyPort(proxyPort);

        AntennaContext context = createContext(new AntennaContext.ContextBuilder(factory), toolConfigBuilder);
        HttpClientConfig config = fetchHttpClientConfig(factory);
        assertThat(config.customObjectMapper()).contains(context.getObjectMapper());
        assertThat(config.proxySettings()).isEqualTo(ProxySettings.useProxy(proxyHost, proxyPort));
        assertThat(context.getHttpClient()).isEqualTo(httpClient);
    }
}
