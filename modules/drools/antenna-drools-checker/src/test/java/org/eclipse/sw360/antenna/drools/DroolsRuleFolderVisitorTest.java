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

package org.eclipse.sw360.antenna.drools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DroolsRuleFolderVisitorTest {

    private static final String RESOURCE_PATH = "../../../../../policies/policies.properties";
    private Path folderPath;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource(RESOURCE_PATH);
        Path path = Paths.get(resource.toURI());
        String resourcesFolderPath = path.getParent().getParent().toString();

        folderPath = Paths.get(resourcesFolderPath, "policies", "");
    }

    @Test
    public void usingFileVisitorOnExistingDirectoryWorksCorrectly() throws Exception {
        DroolsRuleFolderVisitor collectRuleFiles = new DroolsRuleFolderVisitor();
        Files.walkFileTree(folderPath, collectRuleFiles);
        List<File> collectedFiles = collectRuleFiles.getRuleFiles();

        assertThat(collectedFiles).hasSize(2);
        assertThat(collectedFiles.stream().map(File::getAbsolutePath).filter(s -> s.endsWith("DummyRule.drl"))).hasSize(1);
    }

    @Test
    public void usingFileVisitorOnMissingDirectoryDoesNotThrow() throws Exception {
        DroolsRuleFolderVisitor collectRuleFiles = new DroolsRuleFolderVisitor();
        Files.walkFileTree(Paths.get("some/path"), collectRuleFiles);
        List<File> collectedFiles = collectRuleFiles.getRuleFiles();

        assertThat(collectedFiles).hasSize(0);
    }
}
