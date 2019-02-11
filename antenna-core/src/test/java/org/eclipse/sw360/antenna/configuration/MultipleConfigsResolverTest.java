/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.configuration;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.xml.XMLValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MultipleConfigsResolverTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock
    protected ToolConfiguration toolConfigMock = mock(ToolConfiguration.class);

    private List<File> configs = new ArrayList<>();

    private Configuration mergedConfigs;
    private MultipleConfigsResolver resolver;

    @Before
    public void init() throws URISyntaxException, IOException, AntennaConfigurationException {
        URL configUrl = MultipleConfigsResolverTest.class.getResource("/antennaconf.xml");
        URL compareUrl = MultipleConfigsResolverTest.class.getResource("/configCompare.xml");
        XMLValidator validator = new XMLValidator();

        File configuration = new File(configUrl.toURI());
        File compareConfig = new File(compareUrl.toURI());
        this.resolver = new MultipleConfigsResolver();
        configs.add(configuration);
        configs.add(compareConfig);

        when(toolConfigMock.getConfigFiles()).thenReturn(configs);
        when(toolConfigMock.getEncoding()).thenReturn(Charset.forName("UTF-8"));
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(temporaryFolder.newFolder("target").toPath());

        // TODO: the tests currently fail
        this.mergedConfigs = resolver.resolveConfigs(toolConfigMock, false);
    }

    @After
    public void after() {
        verify(toolConfigMock, atLeast(0)).getConfigFiles();
        verify(toolConfigMock, atLeast(0)).getConfigFileUris();
        verify(toolConfigMock, atLeast(0)).getEncoding();
        verify(toolConfigMock, atLeast(0)).getAntennaTargetDirectory();
    }

    @Test
    public void testOverride() {
        assertThat(this.mergedConfigs.getOverride().size()).isEqualTo(0);
        List<ProcessingMessage> messageList = resolver.getReporter().getProcessingReport().getMessageList();
        assertThat(messageList.get(0).getMessage().contains("director-ant.jar"));
        assertThat(messageList.size()).isEqualTo(2);
    }

    @Test
    public void testAddArtifact() {
        assertThat(this.mergedConfigs.getAddArtifact().size()).isEqualTo(3);
    }

    @Test
    public void testRemoveArtifact() {
        assertThat(this.mergedConfigs.getRemoveArtifact().size()).isEqualTo(2);
    }

    @Test
    public void testIgnoreForSourceResolving() {
        assertThat(this.mergedConfigs.getIgnoreForSourceResolving().size()).isEqualTo(1);
    }

    @Test
    public void testFinalLicenses() {
        assertThat(this.mergedConfigs.getFinalLicenses().size()).isEqualTo(1);
    }

    @Test
    public void testValidationOfSources() {
        assertThat(this.mergedConfigs.getValidForIncompleteSources().size()).isEqualTo(2);
        assertThat(this.mergedConfigs.getValidForMissingSources().size()).isEqualTo(2);
        assertThat(this.mergedConfigs.getValidForIncompleteSources())
                .isEqualTo(this.mergedConfigs.getValidForMissingSources());
    }

    @Test
    public void testFailOn() {
        assertThat(this.mergedConfigs.isFailOnIncompleteSources()).isFalse();
        assertThat(this.mergedConfigs.isFailOnMissingSources()).isTrue();
    }

}
