/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.p2;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class UriCheckTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isRepositoryUriValidReturnsTrueForExistingDirectory() throws IOException {
        File existingFolder = folder.newFolder();

        assertThat(UriCheck.isRepositoryUriValid(existingFolder.toURI())).isTrue();
    }

    @Test
    public void isRepositoryUriValidReturnsFalseForNonexistingDirectory() throws IOException {
        File nonExistingFolder = new File(Paths.get(folder.getRoot().toString(), "nonexistant").toUri());

        assertThat(UriCheck.isRepositoryUriValid(nonExistingFolder.toURI())).isFalse();
    }

    @Test
    public void isRepositoryUriValidReturnsReturnsTrueForValidURL() throws IOException, URISyntaxException {
        URI validUrlHttpUri = new URL("http://www.example.org").toURI();
        URI validUrlHttpsUri = new URL("https://www.example.org").toURI();

        assertThat(UriCheck.isRepositoryUriValid(validUrlHttpUri)).isTrue();
        assertThat(UriCheck.isRepositoryUriValid(validUrlHttpsUri)).isTrue();
    }
}
