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
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360AttachmentUtilsTest {
    /**
     * A file used by test cases.
     */
    private static final String TEST_FILE = "/__files/all_releases.json";

    /**
     * Returns a path to the test file from the test resources.
     *
     * @return the path to the test file
     * @throws URISyntaxException if the file cannot be resolved
     */
    private static Path testFile() throws URISyntaxException {
        return Paths.get(SW360AttachmentUtilsTest.class.getResource(TEST_FILE).toURI());
    }

    @Test
    public void testCalculateHash() throws NoSuchAlgorithmException, URISyntaxException {
        // manually calculated using the Linux md5sum command line tool
        final String expectedMd5 = "457b4178ee4d1129603a1a6465c8e9c1";
        MessageDigest digest = MessageDigest.getInstance("md5");

        String hash = SW360AttachmentUtils.calculateHash(testFile(), digest);
        assertThat(hash).isEqualTo(expectedMd5);
    }

    @Test(expected = SW360ClientException.class)
    public void testCalculateHashException() throws NoSuchAlgorithmException {
        Path file = Paths.get("non/existing/file.txt");
        MessageDigest digest = MessageDigest.getInstance("md5");

        SW360AttachmentUtils.calculateHash(file, digest);
    }

    @Test
    public void testCalculateSha1Hash() throws URISyntaxException {
        // manually calculated using the Linux sha1sum command line tool
        final String expSha1Hash = "7a5daedffafd0be187c351968592fefee4f648f4";
        String sha1Hash = SW360AttachmentUtils.calculateSha1Hash(testFile());

        assertThat(sha1Hash).isEqualTo(expSha1Hash);
    }
}
