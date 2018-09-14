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
package org.eclipse.sw360.antenna.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * ScanFileFinderTest
 */
public class ScanFileFinderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void findWarPattern() throws Exception {
        File baseDir = folder.newFolder("da_base");
        File fooDir = new File(baseDir, "foo");
        fooDir.mkdir();

        File f1 = new File(baseDir, "a.war");
        File f2 = new File(baseDir, "b.zip");
        File f3 = new File(fooDir, "c.war");
        f1.createNewFile();
        f2.createNewFile();
        f3.createNewFile();

        // Collection<File> files = ScanFileFinder.collectFiles(baseDir,
        // "*.war");
        // assertThat(files).hasSize(2);
    }
}
