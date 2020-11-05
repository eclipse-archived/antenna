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

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState.ClearingState;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class SW360UpdaterParameterizedTest {
    private static final String CLEARING_DOC = "clearing.doc";
    private static final String CLEARING_DOC_DIR = "clearing_documents";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final SW360Configuration configurationMock = mock(SW360Configuration.class);
    private final SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);

    private final ClearingState clearingState;
    private final boolean clearingDocAvailable;
    private final boolean expectUpload;
    private final boolean overwriteSW360Data;

    public SW360UpdaterParameterizedTest(@Nullable ClearingState clearingState, boolean clearingDocAvailable, boolean expectUpload, boolean overwriteSW360Data) {
        this.clearingState = clearingState;
        this.clearingDocAvailable = clearingDocAvailable;
        this.expectUpload = expectUpload;
        this.overwriteSW360Data = overwriteSW360Data;
    }

    @Parameterized.Parameters(name = "{0}_{1}_{2}_{3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {ClearingState.OSM_APPROVED, true, true, false},
                {ClearingState.EXTERNAL_SOURCE, true, true, false},
                {ClearingState.AUTO_EXTRACT, true, true, false},
                {ClearingState.PROJECT_APPROVED, true, true, false},
                {ClearingState.INITIAL, true, false, false},
                {ClearingState.WORK_IN_PROGRESS, false, false, true},
                {null, true, false, false},
                {ClearingState.OSM_APPROVED, false, true, false},
                {ClearingState.EXTERNAL_SOURCE, false, true, false},
                {ClearingState.AUTO_EXTRACT, false, true, false},
                {ClearingState.PROJECT_APPROVED, false, true, false},
                {ClearingState.INITIAL, false, false, false},
                {ClearingState.WORK_IN_PROGRESS, true, false, true},
                {null, false, false, false}

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
        String clearingDocPath = "";
        if (clearingDocAvailable) {
            File clearingDoc = folder.newFile(CLEARING_DOC);
            clearingDocPath = clearingDoc.getPath();
        }
        Path csvFile = SW360TestUtils.writeCsvFile(folder, sourceAttachment.toString(), clearingState, clearingDocPath);

        when(configurationMock.getBaseDir())
                .thenReturn(csvFile.getParent());
        when(configurationMock.getCsvFilePath())
                .thenReturn(csvFile);
        when(configurationMock.getSourcesPath())
                .thenReturn(sourceAttachment.getParent());
        SW360TestUtils.initConfigProperties(configurationMock, propertiesMap);
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

    private Path getClearingDocDir() throws IOException {
        return Files.createDirectories(getTargetDir().resolve(CLEARING_DOC_DIR));
    }

    @Test
    public void testExecute() throws IOException {
        runUpdaterTest(getConfigProperties(), false, true, false, true);
    }

    @Test
    public void testExecuteWithSourcesCleanup() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARED_SOURCES, String.valueOf(true));

        runUpdaterTest(configProperties, true, true, false, true);
    }

    @Test
    public void testExecuteWithClearingDocumentRemoved() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARING_DOCS, String.valueOf(true));

        runUpdaterTest(configProperties, false, true, true, true);
    }

    @Test
    public void testErrorsWhenRemovingSourceFilesAreIgnored() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARED_SOURCES, String.valueOf(true));

        runUpdaterTest(configProperties, false, false, false, true);
    }

    @Test
    public void testFilesAreNotRemovedWhenUploadFails() throws IOException {
        Map<String, String> configProperties = getConfigProperties();
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARED_SOURCES, String.valueOf(true));
        configProperties.put(SW360Updater.PROP_REMOVE_CLEARING_DOCS, String.valueOf(true));

        runUpdaterTest(configProperties, false, true, false, false);
    }

    private void runUpdaterTest(Map<String, String> config, boolean expectSourceRemoved, boolean attachmentExists,
                                boolean expectClearingDocRemoved, boolean uploadsSuccessful)
            throws IOException {
        Path sourceAttachment = createSourceAttachment();
        initBasicConfiguration(sourceAttachment, config);

        initConnectionConfiguration();

        Path clearingDocumentPath = getAvailableOrGeneratedClearingDocumentPath();
        Map<AttachmentUploadRequest.Item, Throwable> uploadFailures = new HashMap<>();
        if(!uploadsSuccessful) {
            uploadFailures.put(toItem(sourceAttachment), new IOException("Failure 1"));
            uploadFailures.put(toItem(clearingDocumentPath), new IOException("Failure 2"));
        }
        SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);
        SW360Release release = new SW360Release();
        if (clearingState != null) {
            release.setClearingState(clearingState.name());
        }
        when(updater.artifactToReleaseWithUploads(any(), any(), anyMap()))
                .thenAnswer((Answer<AttachmentUploadResult<SW360Release>>) invocationOnMock -> {
                    deleteSourceFileIfNotAttachmentExists(attachmentExists, sourceAttachment);
                    return AttachmentUploadResult.newResult(release, Collections.emptySet(), uploadFailures);
                })
                .thenThrow(new SW360ClientException("Boo"));

        ClearingReportGenerator generator = mock(ClearingReportGenerator.class);
        Path clearingDocDir = getClearingDocDir();
        Path clearingDoc = clearingDocDir.resolve(CLEARING_DOC);
        Files.write(clearingDoc, "The clearing document".getBytes(StandardCharsets.UTF_8));
        when(generator.createClearingDocument(any(), eq(clearingDocDir)))
                .thenReturn(clearingDoc);

        SW360Updater sw360Updater = new SW360Updater(updater, configurationMock, generator);

        if (clearingState == null || clearingState == ClearingState.INITIAL || clearingState == ClearingState.WORK_IN_PROGRESS) {
            sw360Updater.execute();
        } else {
            // the update exception will be thrown only in some clearing states
            Assertions.assertThatThrownBy(sw360Updater::execute)
                    .isInstanceOf(SW360ClientException.class);
        }

        Map<Path, SW360AttachmentType> testAttachmentMap = createExpectedAttachmentMap();

        if (expectUpload && !clearingDocAvailable) {
            ArgumentCaptor<SW360Release> captorAllReleases = ArgumentCaptor.forClass(SW360Release.class);
            ArgumentCaptor<SW360Release> captorClearedReleases = ArgumentCaptor.forClass(SW360Release.class);
            verify(updater, times(2))
                    .artifactToReleaseWithUploads(any(), captorAllReleases.capture(), anyMap());
            verify(generator, times(2))
                    .createClearingDocument(captorClearedReleases.capture(), eq(clearingDocDir));
            assertThat(captorClearedReleases.getAllValues()).containsOnlyElementsOf(captorAllReleases.getAllValues());
        }
        if (expectUpload) {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<Path, SW360AttachmentType>> captor = ArgumentCaptor.forClass(Map.class);
            verify(updater, times(2)).artifactToReleaseWithUploads(any(), any(), captor.capture());
            assertThat(captor.getAllValues().stream().anyMatch(map -> map.equals(testAttachmentMap))).isTrue();
        } else {
            deleteSourceFileIfNotAttachmentExists(attachmentExists, sourceAttachment);
            verify(updater, never()).artifactToReleaseWithUploads(any(), any(), anyMap());

            if(clearingState == ClearingState.WORK_IN_PROGRESS) {
                verify(updater, times(2)).artifactToReleaseInSW360(any(), any(), eq(overwriteSW360Data));
            }
        }

        boolean sourcePresent = Files.exists(sourceAttachment);
        assertThat(sourcePresent).isEqualTo((!expectUpload || !expectSourceRemoved) && attachmentExists);

        boolean clearingDocPresent = Files.exists(clearingDocumentPath);
        assertThat(clearingDocPresent).isEqualTo(!expectUpload || !expectClearingDocRemoved);
    }

    /**
     * This deletes the file at the given path if the boolean
     * given is false.
     *
     * @param attachmentExists determines if file should exist or not
     * @param sourceAttachment path to file that might get deleted
     */
    private void deleteSourceFileIfNotAttachmentExists(boolean attachmentExists, Path sourceAttachment) throws IOException {
        if (!attachmentExists) {
            // Provoke an error when deleting the attachment. Note that the attachment must be
            // present when the CSV file is read; so we delete it afterwards.
            Files.delete(sourceAttachment);
        }
    }

    private Path getAvailableOrGeneratedClearingDocumentPath() throws IOException {
        return clearingDocAvailable ? configurationMock.getBaseDir().resolve(CLEARING_DOC) :
                getClearingDocDir().resolve(CLEARING_DOC);
    }

    private Map<Path, SW360AttachmentType> createExpectedAttachmentMap() throws IOException {
        if (clearingDocAvailable || expectUpload) {
            return Collections
                    .singletonMap(getAvailableOrGeneratedClearingDocumentPath(), SW360AttachmentType.CLEARING_REPORT);
        }

        return Collections.emptyMap();
    }

    /**
     * Generates an upload request item for the path provided. The attachment
     * type is not relevant here.
     *
     * @param p the path of the item
     * @return the item with this path
     */
    private static AttachmentUploadRequest.Item toItem(Path p) {
        return new AttachmentUploadRequest.Item(p, SW360AttachmentType.SOURCE);
    }
}
