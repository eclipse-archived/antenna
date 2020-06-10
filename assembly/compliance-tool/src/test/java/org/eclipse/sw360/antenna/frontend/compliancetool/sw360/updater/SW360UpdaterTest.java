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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class SW360UpdaterTest {
    private static final String CLEARING_DOC = "clearing.doc";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final SW360Configuration configurationMock = mock(SW360Configuration.class);
    private final SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);

    private final String clearingState;
    private final boolean clearingDocAvailable;
    private final boolean expectUpload;

    public SW360UpdaterTest(String clearingState, boolean clearingDocAvailable, boolean expectUpload) {
        this.clearingState = clearingState;
        this.clearingDocAvailable = clearingDocAvailable;
        this.expectUpload = expectUpload;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"OSM_APPROVED", true, true},
                {"EXTERNAL_SOURCE", true, true},
                {"AUTO_EXTRACT", true, true},
                {"PROJECT_APPROVED", true, true},
                {"INITIAL", true, false},
                {"", true, false},
                {"OSM_APPROVED", false, true},
                {"EXTERNAL_SOURCE", false, true},
                {"AUTO_EXTRACT", false, true},
                {"PROJECT_APPROVED", false, true},
                {"INITIAL", false, false},
                {"", false, false}

        });
    }

    private void initConnectionConfiguration() {
        SW360Connection connectionConfiguration = mock(SW360Connection.class);

        when(connectionConfiguration.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);
        when(configurationMock.getConnection())
                .thenReturn(connectionConfiguration);
    }

    private void initBasicConfiguration(Path sourceAttachment, Map<String, String> propertiesMap) throws IOException {
        Path csvFile = writeCsvFile(sourceAttachment.toString());

        when(configurationMock.getProperties())
                .thenReturn(propertiesMap);
        when(configurationMock.getBaseDir())
                .thenReturn(csvFile.getParent());
        when(configurationMock.getCsvFilePath())
                .thenReturn(csvFile);
        when(configurationMock.getTargetDir())
                .thenReturn(getTargetDir());
        when(configurationMock.getSourcesPath())
                .thenReturn(sourceAttachment.getParent());
    }

    private Map<String, String> getConfigProperties() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        return ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
    }

    private Path createSourceAttachment() throws IOException {
        Path sourcesPath = folder.newFolder("sources").toPath();
        Path sourceAttachment = sourcesPath.resolve("testSources.zip");
        return Files.write(sourceAttachment, "The whole source code".getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    private Path getTargetDir() {
        return folder.getRoot().toPath();
    }

    @Test(expected = NullPointerException.class)
    public void testUpdaterImplMustNotBeNull() {
        new SW360Updater(null, configurationMock, mock(ClearingReportGenerator.class));
    }

    @Test(expected = NullPointerException.class)
    public void testConfigurationMustNotBeNull() {
        new SW360Updater(mock(SW360UpdaterImpl.class), null, mock(ClearingReportGenerator.class));
    }

    @Test(expected = NullPointerException.class)
    public void testClearingReportGeneratorMustNotBeNull() {
        new SW360Updater(mock(SW360UpdaterImpl.class), configurationMock, null);
    }

    @Test
    public void testExecute() throws IOException {
        runUpdaterTest(getConfigProperties(), false, true, false);
    }

    @Test
    public void testExecuteWithSourcesCleanup() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARED_SOURCES, String.valueOf(true));

        runUpdaterTest(configProperties, true, true, false);
    }

    @Test
    public void testExecuteWithClearingDocumentRemoved() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARING_DOCS, String.valueOf(true));

        runUpdaterTest(configProperties, false, true, true);
    }

    @Test
    public void testErrorsWhenRemovingSourceFilesAreIgnored() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARED_SOURCES, String.valueOf(true));

        runUpdaterTest(configProperties, false, false, false);
    }

    private void runUpdaterTest(Map<String, String> config, boolean expectSourceRemoved, boolean attachmentExists, boolean expectClearingDocRemoved)
            throws IOException {
        Path sourceAttachment = createSourceAttachment();
        initBasicConfiguration(sourceAttachment, config);

        initConnectionConfiguration();

        SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);
        SW360Release release = new SW360Release();
        release.setClearingState(clearingState);
        when(updater.artifactToReleaseInSW360(any(), any()))
                .thenAnswer((Answer<SW360Release>) invocationOnMock -> {
                    deleteSourceAttachmentIfAttachmentExists(attachmentExists, sourceAttachment);
                    return release;
                })
                .thenThrow(new SW360ClientException("Boo"));

        ClearingReportGenerator generator = mock(ClearingReportGenerator.class);
        Path clearingDoc = getTargetDir().resolve(CLEARING_DOC);
        Files.write(clearingDoc, "The clearing document".getBytes(StandardCharsets.UTF_8));
        when(generator.createClearingDocument(any(), any()))
                .thenReturn(clearingDoc);

        SW360Updater sw360Updater = new SW360Updater(updater, configurationMock, generator);

        sw360Updater.execute();

        Map<Path, SW360AttachmentType> testAttachmentMap = createExpectedAttachmentMap();

        if (expectUpload && !clearingDocAvailable) {
            verify(generator).createClearingDocument(release, getTargetDir());
        }
        @SuppressWarnings("unchecked")
        ArgumentCaptor<AttachmentUploadRequest<SW360Release>> captor = ArgumentCaptor.forClass(AttachmentUploadRequest.class);
        verify(releaseClientAdapter, times(expectUpload ? 1 : 0)).uploadAttachments(captor.capture());
        if (expectUpload) {
            verify(updater, times(2)).artifactToReleaseInSW360(any(), any());
            AttachmentUploadRequest<SW360Release> uploadRequest = captor.getValue();
            List<AttachmentUploadRequest.Item> expItems = testAttachmentMap.entrySet().stream()
                    .map(entry -> new AttachmentUploadRequest.Item(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            assertThat(uploadRequest.getItems()).containsAll(expItems);
        } else {
            deleteSourceAttachmentIfAttachmentExists(attachmentExists, sourceAttachment);
            verify(updater, never()).artifactToReleaseInSW360(any(), any());
        }

        boolean sourcePresent = Files.exists(sourceAttachment);
        assertThat(sourcePresent).isEqualTo((!expectUpload || !expectSourceRemoved) && attachmentExists);

        boolean clearingDocPresent = Files.exists(clearingDoc);
        assertThat(clearingDocPresent).isEqualTo(!expectUpload || !expectClearingDocRemoved);
    }

    /**
     * deletes the file of the given path if the given boolean is true
     * @param attachmentExists determines if the file at the given path should remain existing
     * @param sourceAttachment path to file that might get deleted
     * @throws IOException
     */
    private void deleteSourceAttachmentIfAttachmentExists(boolean attachmentExists, Path sourceAttachment) throws IOException {
        if (!attachmentExists) {
            // Provoke an error when deleting the attachment. Note that the attachment must be
            // present when the CSV file is read; so we delete it afterwards.
            Files.delete(sourceAttachment);
        }
    }

    private Map<Path, SW360AttachmentType> createExpectedAttachmentMap() {
        if (clearingDocAvailable) {
            return Collections
                    .singletonMap(configurationMock.getBaseDir().resolve(CLEARING_DOC), SW360AttachmentType.CLEARING_REPORT);
        } else if (expectUpload) {
            return Collections
                    .singletonMap(getTargetDir().resolve(CLEARING_DOC), SW360AttachmentType.CLEARING_REPORT);
        }

        return Collections.emptyMap();
    }

    private Path writeCsvFile(String sourceAttachment) throws IOException {
        final File tempCsvFile = folder.newFile("test.csv");
        String clearingDocPath = "";
        if (clearingDocAvailable) {
            File clearingDoc = folder.newFile(CLEARING_DOC);
            clearingDocPath = clearingDoc.getPath();
        }
        String csvContent = String.format("Artifact Id,Group Id,Version,Coordinate Type,Clearing State,Clearing Document,File Name%s" +
                        "test,test,x.x.x,mvn,%s,%s,%s%s" +
                        "error,error,y.y.y,mvn,%s,%s,%s", System.lineSeparator(), clearingState, clearingDocPath,
                sourceAttachment, System.lineSeparator(), clearingState, clearingDocPath, System.lineSeparator());
        Files.write(tempCsvFile.toPath(), csvContent.getBytes(StandardCharsets.UTF_8));
        return tempCsvFile.toPath();
    }
}
