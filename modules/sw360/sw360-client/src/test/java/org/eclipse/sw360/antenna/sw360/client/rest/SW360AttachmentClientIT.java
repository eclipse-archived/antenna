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
package org.eclipse.sw360.antenna.sw360.client.rest;

import org.eclipse.sw360.antenna.http.utils.FailedRequestException;
import org.eclipse.sw360.antenna.http.utils.HttpConstants;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.http.utils.HttpUtils.waitFor;

public class SW360AttachmentClientIT extends AbstractMockServerTest {
    /**
     * The name of the directory where downloaded attachments are stored.
     */
    private static final String DOWNLOAD_DIRECTORY = "downloads";

    /**
     * File name used for a test attachment.
     */
    private static final String ATTACHMENT_FILE = "testAttachment.json";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private SW360ReleaseClient attachmentClient;

    @Before
    public void setUp() {
        attachmentClient = new SW360ReleaseClient(createClientConfig(), createMockTokenProvider());
        prepareAccessTokens(attachmentClient.getTokenProvider(), CompletableFuture.completedFuture(ACCESS_TOKEN));
    }

    /**
     * Determines the directory to download attachments as a sub folder of the
     * temporary folder.
     *
     * @return the attachment download directory
     */
    private Path getDownloadDir() {
        return temporaryFolder.getRoot().toPath().resolve(DOWNLOAD_DIRECTORY);
    }

    @Test
    public void testUploadAttachment() throws URISyntaxException, IOException {
        String urlPath = "/releases/rel1234567890";
        String selfUrl = wireMockRule.baseUrl() + urlPath;
        Path attachmentPath = Paths.get(resolveTestFileURL("license.json").toURI());
        byte[] attachmentContent = Files.readAllBytes(attachmentPath);
        SW360Attachment attachment = new SW360Attachment(attachmentPath, SW360AttachmentType.DOCUMENT);
        SW360Release release = new SW360Release();
        release.get_Links().setSelf(new Self(selfUrl));

        wireMockRule.stubFor(post(urlPathEqualTo(urlPath + "/attachments"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("attachment")
                                .withHeader("Content-Type", containing("application/json"))
                                .withBody(equalToJson(toJson(attachment)))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("file")
                                .withBody(binaryEqualTo(attachmentContent))
                ).willReturn(aJsonResponse(HttpConstants.STATUS_OK)
                        .withBody(toJson(release))));

        SW360Release modifiedRelease =
                waitFor(attachmentClient.uploadAndAttachAttachment(release, attachmentPath,
                        SW360AttachmentType.DOCUMENT));
        assertThat(modifiedRelease).isEqualTo(release);
        wireMockRule.verify(postRequestedFor(urlPathEqualTo(urlPath + "/attachments")));
    }

    @Test
    public void testUploadAttachmentNonExistingFile() {
        Path attachmentPath = temporaryFolder.getRoot().toPath().resolve("nonExistingFile.txt");
        SW360Release release = new SW360Release();

        extractException(attachmentClient.uploadAndAttachAttachment(release, attachmentPath,
                SW360AttachmentType.DOCUMENT), IOException.class);
        assertThat(wireMockRule.getAllServeEvents()).hasSize(0);
    }

    @Test
    public void testUploadAttachmentError() throws URISyntaxException {
        String urlPath = "/releases/rel1234567890";
        String selfUrl = wireMockRule.baseUrl() + urlPath;
        Path attachmentPath = Paths.get(resolveTestFileURL("license.json").toURI());
        SW360Release release = new SW360Release();
        release.get_Links().setSelf(new Self(selfUrl));
        wireMockRule.stubFor(post(anyUrl())
                .willReturn(aJsonResponse(HttpConstants.STATUS_ERR_BAD_REQUEST)));

        FailedRequestException exception = expectFailedRequest(
                attachmentClient.uploadAndAttachAttachment(release, attachmentPath, SW360AttachmentType.DOCUMENT),
                HttpConstants.STATUS_ERR_BAD_REQUEST);
        assertThat(exception.getTag()).isEqualTo(SW360AttachmentAwareClient.TAG_UPLOAD_ATTACHMENT);
    }

    @Test
    public void testDownloadAttachment() throws IOException {
        final String attachmentID = "test-attachment-id";
        final String itemRef = "/testComponent";
        final String testFile = "project.json";
        Path downloadDir = getDownloadDir();
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/" + attachmentID));
        attachment.setFilename(ATTACHMENT_FILE);
        wireMockRule.stubFor(get(urlPathEqualTo(itemRef + "/attachments/" + attachmentID))
                .withHeader(HttpConstants.HEADER_ACCEPT, equalTo(HttpConstants.CONTENT_OCTET_STREAM))
                .willReturn(aJsonResponse(HttpConstants.STATUS_OK)
                        .withBodyFile(testFile)));

        Path path = waitFor(attachmentClient.downloadAttachment(wireMockRule.baseUrl() + itemRef,
                attachment, downloadDir));
        assertThat(path.getFileName().toString()).isEqualTo(ATTACHMENT_FILE);
        assertThat(path.getParent()).isEqualTo(downloadDir);
        SW360Project expData = readTestJsonFile(resolveTestFileURL(testFile), SW360Project.class);
        SW360Project actData = readTestJsonFile(path.toUri().toURL(), SW360Project.class);
        assertThat(actData).isEqualTo(expData);
    }

    @Test
    public void testDownloadAttachmentPathAlreadyExisting() throws IOException {
        Path downloadDir = getDownloadDir();
        Files.createDirectory(downloadDir);

        testDownloadAttachment();
    }

    @Test
    public void testDownloadAttachmentIfFileIsAlreadyPresent() throws IOException {
        Path attachment = Files.createDirectory(getDownloadDir()).resolve(ATTACHMENT_FILE);
        Files.write(attachment, Arrays.asList("This", "is", "a", "test."));

        testDownloadAttachment();
    }

    @Test
    public void testDownloadAttachmentInvalidDownloadPath() {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() +
                "/test/attachments/notDownloadedAttachment"));
        attachment.setFilename("irrelevant.file");
        Path downloadPath = temporaryFolder.getRoot().toPath().resolve("non").resolve("existing").resolve("path");

        extractException(attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                downloadPath), IOException.class);
        assertThat(wireMockRule.getAllServeEvents()).hasSize(0);
    }

    @Test
    public void testDownloadAttachmentNotFound() {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/unknownAttachment"));
        attachment.setFilename("nonExisting.file");
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ERR_NOT_FOUND)));

        FailedRequestException exception =
                expectFailedRequest(attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                        temporaryFolder.getRoot().toPath().resolve("downloads")),
                        HttpConstants.STATUS_ERR_NOT_FOUND);
        assertThat(exception.getTag()).isEqualTo(SW360AttachmentAwareClient.TAG_DOWNLOAD_ATTACHMENT);
    }

    @Test
    public void testDownloadAttachmentNoContent() throws IOException {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/empty"));
        attachment.setFilename("empty.file");
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_OK)));

        Path path = waitFor(attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                temporaryFolder.getRoot().toPath().resolve("downloads")));
        assertThat(Files.size(path)).isEqualTo(0);
        assertThat(Files.exists(path)).isTrue();
    }
}
