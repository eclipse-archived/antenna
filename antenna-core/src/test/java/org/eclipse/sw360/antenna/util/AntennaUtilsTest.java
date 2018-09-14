/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assume.*;

public class AntennaUtilsTest {

    private void onlyForWindows() {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        assumeTrue(isWindows);
    }

    private void onlyForNonWindows() {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        assumeTrue(!isWindows);
    }

    @Test
    public void getJarPathTestStripsPrefix() throws  Exception {
        String fileBasename = "AntennaUtilsTest_testfile.jar";
        final URL url = this.getClass().getClassLoader().getResource(fileBasename);

        final Path jarPath = AntennaUtils.getJarPath(url);

        assertThat(jarPath.toString(), not(startsWith("file:")));
        assertThat(jarPath.toString(), not(startsWith("jar:")));
        assertThat(jarPath.getFileName().toString(), is(fileBasename));
        assertThat(jarPath.toFile().exists(), is(true));
    }

    @Test
    public void getJarPathTestDoesWorkOnWinBackwardSlashes() throws  Exception {
        onlyForWindows();

        final URL url = new URL("file:///C:/Documents%20and%20Settings/antenna/someFile.jar/child.war/subChild.jar");

        final Path jarPath = AntennaUtils.getJarPath(url);

        assertThat(jarPath, isPath("C:\\Documents and Settings\\antenna\\someFile.jar"));
    }

    @Test
    public void getJarPathTestDoesWorkOnWinForwardSlashes() throws  Exception {
        onlyForWindows();

        final URL url = new URL("file:///C:/Documents%20and%20Settings/antenna/someFile.jar/child.war/subChild.jar");

        final Path jarPath = AntennaUtils.getJarPath(url);

        assertThat(jarPath, isPath("C:\\Documents and Settings\\antenna\\someFile.jar"));
    }

    @Test
    public void getJarPathTestWithoutChilds() throws Exception {
        Path filePath = Paths.get("/some/path/to/file.jar").toAbsolutePath();

        final Path jarPath = AntennaUtils.getJarPath(filePath);

        assertThat(jarPath, isPath("/some/path/to/file.jar"));
    }

    @Test(expected = AntennaExecutionException.class)
    public void getJarRelativePathTestExpectedFail() throws Exception {
        Path filePath = Paths.get("some/path/to/file.jar");

        AntennaUtils.getJarPath(filePath);
    }

    @Test
    public void getJarPathTestWithJarFile() throws Exception {
        Path filePath = Paths.get("/some/path/to/file.jar/child.war/subChild.jar").toAbsolutePath();

        final Path jarPath = AntennaUtils.getJarPath(filePath);

        assertThat(jarPath, isPath("/some/path/to/file.jar"));
    }

    @Test
    public void getJarPathTestWithFile() throws Exception {
        Path filePath = Paths.get("/some/path/to/file.jar/child.war/subChild.jar").toAbsolutePath();

        final Path jarPath = AntennaUtils.getJarPath(filePath);

        assertThat(jarPath, isPath("/some/path/to/file.jar"));
    }

    @Test
    public void getJarPathIteratorFromPathTest() throws Exception {
        String filePath = "/some/path/to/file.jar/child.war/subChild.jar";

        final Iterator<Path> iterator = AntennaUtils.getJarPathIteratorFromPath(Paths.get(filePath).toAbsolutePath());

        Path val;
        assertThat(iterator.hasNext(), is(true));
        val = iterator.next();
        assertThat(val, isPath("/some/path/to/file.jar"));
        assertThat(iterator.hasNext(), is(true));
        val = iterator.next();
        assertThat(val, isPath("/child.war", true));
        assertThat(iterator.hasNext(), is(true));
        val = iterator.next();
        assertThat(val, isPath("/subChild.jar", true));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void computeInnerReplacementJarPathTest() throws Exception {
        Path filePath = Paths.get("/some/path/to/file.jar/child.war/subChild.jar").toAbsolutePath();

        Path computed = AntennaUtils.computeInnerReplacementJarPath(filePath);

        assertThat(computed, isPath("/some/path/to/file_jar/child_war/subChild.jar"));
    }

    @Test
    public void computeJarPathFromURLWithExclemationMarkOnWin() throws Exception {
        onlyForWindows();

        final URL url = new URL("jar:file:/C:/some/path/to/file.jar!/some/inner/thing.class");

        final Path jarPath = AntennaUtils.getJarPath(url);

        assertThat(jarPath, isPath("C:\\some\\path\\to\\file.jar"));
    }

    @Test
    public void computeJarPathFromURLWithExclemationMark() throws Exception {
        onlyForNonWindows();
        final URL url = new URL("jar:file://"+Paths.get("/some/path/to/file.jar").toAbsolutePath().toString()+"!/some/inner/thing.class");

        final Path jarPath = AntennaUtils.getJarPath(url);

        assertThat(jarPath, isPath("/some/path/to/file.jar"));
    }

    private Matcher<Path> isPath(String string){
        return isPath(string, false);
    }

    private Matcher<Path> isPath(String string, boolean notAbsolutefied){
        if(notAbsolutefied){
            return is(Paths.get(string));
        }
        return is(Paths.get(string).toAbsolutePath());
    }
}
