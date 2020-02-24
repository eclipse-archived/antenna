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
package org.eclipse.sw360.antenna.sw360.rest;

import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360AttachmentClientIT extends AbstractMockServerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private SW360ReleaseClient attachmentClient;

    @Before
    public void setUp() {
        attachmentClient = new SW360ReleaseClient(wireMockRule.baseUrl(), createRestTemplate());
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
                                .withHeader("Content-Type", equalTo("application/json"))
                                .withBody(equalToJson(toJson(attachment)))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("file")
                                .withBody(binaryEqualTo(attachmentContent))
                ).willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBody(toJson(release))));

        SW360Release modifiedRelease =
                attachmentClient.uploadAndAttachAttachment(release, attachmentPath, SW360AttachmentType.DOCUMENT,
                        new HttpHeaders());
        assertThat(modifiedRelease).isEqualTo(release);
        wireMockRule.verify(postRequestedFor(urlPathEqualTo(urlPath + "/attachments")));
    }

    @Test
    public void testUploadAttachmentNonExistingFile() {
        Path attachmentPath = temporaryFolder.getRoot().toPath().resolve("nonExistingFile.txt");
        SW360Release release = new SW360Release();

        SW360Release modifiedRelease =
                attachmentClient.uploadAndAttachAttachment(release, attachmentPath, SW360AttachmentType.DOCUMENT,
                        new HttpHeaders());
        assertThat(modifiedRelease).isEqualTo(release);
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
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        SW360Release modifiedRelease =
                attachmentClient.uploadAndAttachAttachment(release, attachmentPath, SW360AttachmentType.DOCUMENT,
                        new HttpHeaders());
        assertThat(modifiedRelease).isEqualTo(release);
    }

    @Test
    public void testDownloadAttachment() throws IOException {
        final String attachmentID = "test-attachment-id";
        final String fileName = "testAttachment.json";
        final String itemRef = "/testComponent";
        final String testFile = "project.json";
        Path downloadDir = temporaryFolder.getRoot().toPath().resolve("downloads");
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/" + attachmentID));
        attachment.setFilename(fileName);
        wireMockRule.stubFor(get(urlPathEqualTo(itemRef + "/attachments/" + attachmentID))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile(testFile)));

        Path path = assertPresent(attachmentClient.downloadAttachment(wireMockRule.baseUrl() + itemRef,
                attachment, downloadDir, new HttpHeaders()));
        assertThat(path.getFileName().toString()).isEqualTo(fileName);
        assertThat(path.getParent()).isEqualTo(downloadDir);
        SW360Project expData = readTestJsonFile(resolveTestFileURL(testFile), SW360Project.class);
        SW360Project actData = readTestJsonFile(path.toUri().toURL(), SW360Project.class);
        assertThat(actData).isEqualTo(expData);
    }

    @Test
    public void testDownloadAttachmentInvalidDownloadPath() {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() +
                "/test/attachments/notDownloadedAttachment"));
        attachment.setFilename("irrelevant.file");
        Path downloadPath = temporaryFolder.getRoot().toPath().resolve("non").resolve("existing").resolve("path");

        Optional<Path> optPath = attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                downloadPath, new HttpHeaders());
        assertThat(optPath).isNotPresent();
        assertThat(wireMockRule.getAllServeEvents()).hasSize(0);
    }

    @Test
    public void testDownloadAttachmentNotFound() {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/unknownAttachment"));
        attachment.setFilename("nonExisting.file");
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));

        Optional<Path> optPath = attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                temporaryFolder.getRoot().toPath().resolve("downloads"), new HttpHeaders());
        assertThat(optPath).isNotPresent();
    }

    @Test
    public void testDownloadAttachmentNoContent() {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.get_Links().setSelf(new Self(wireMockRule.baseUrl() + "/test/attachments/empty"));
        attachment.setFilename("empty.file");
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK)));

        Optional<Path> optPath = attachmentClient.downloadAttachment(wireMockRule.baseUrl(), attachment,
                temporaryFolder.getRoot().toPath().resolve("downloads"), new HttpHeaders());
        assertThat(optPath).isNotPresent();
    }
}
