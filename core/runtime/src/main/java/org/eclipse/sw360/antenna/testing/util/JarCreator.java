/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.testing.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class JarCreator {
    private Path workspace;

    public static final String jarWithManifestName = "testWithManifest.jar";
    public static final String jarWithoutManifestName = "testWithoutManifest.jar";
    public static final String jarWithSourcesName = "JarWithArtifactSource.jar";
    public static final String jarInJarName ="JarInJarWM.jar";
    public static final String jarInFoldersName = "META-INF/libs/testWithManifest.jar";
    public static final String jarInJarInJarName ="JarInJarInJarWM.jar";
    public static final String testManifestSymbolicName = "org.test.someorga";
    public static final String testManifestVersion = "2.2.3-SNAPSHOT";
    public Path jarWithManifestPath;
    public Path jarWithoutManifestPath;
    public Path jarWithSourcesPath;
    public Path jarjarjarPath;

    public static final Manifest manifest;
    static {
        manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Bundle-SymbolicName", testManifestSymbolicName +";singleton:=true");
        manifest.getMainAttributes().putValue("Bundle-Version", testManifestVersion);
    }

    public JarCreator() throws IOException {
        this.workspace = Files.createTempDirectory("testRepo");
    }

    public Path getWorkspace() {
        return workspace;
    }

    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(this.workspace.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not clean up. Deletion of test project failed.");
        }
    }

    public Path createJarWithManifest() throws IOException {
        return createJarWithManifest(jarWithManifestName);
    }

    public Path createJarWithManifest(String name) throws IOException {
        jarWithManifestPath = workspace.resolve(name);
        File file = jarWithManifestPath.toFile();
        if(file.exists()){
            return jarWithManifestPath;
        }
        if (jarWithManifestPath.getParent() != null) {
            Path parent = Optional.ofNullable(jarWithManifestPath.getParent())
                    .orElseThrow(() -> new  ExecutionException("The JarWithManifest=[" + jarWithManifestPath + "] should have a parent"));
            Files.createDirectories(parent);
        }
        try (FileOutputStream fileOutput = new FileOutputStream(file);
            JarOutputStream jarOutput = new JarOutputStream(fileOutput, manifest) ) {
            jarOutput.flush();
        }
        return jarWithManifestPath;
    }

    public Path createJarWithoutManifest() throws IOException {
        jarWithoutManifestPath = workspace.resolve(jarWithoutManifestName);
        File file = jarWithoutManifestPath.toFile();
        if(file.exists()){
            return jarWithoutManifestPath;
        }
        try (FileOutputStream fileOutput = new FileOutputStream(file);
            JarOutputStream jarOutput = new JarOutputStream(fileOutput)) {
            ZipEntry ze = new ZipEntry("empty");
            jarOutput.putNextEntry(ze);
            jarOutput.closeEntry();
            jarOutput.flush();
        }

        return jarWithoutManifestPath;
    }

    public File createJarWithSource() throws IOException {
        jarWithSourcesPath = workspace.resolve(jarWithSourcesName);
        File file = jarWithSourcesPath.toFile();
        if(file.exists()){
            return file;
        }
        try (FileOutputStream fileOutput = new FileOutputStream(file);
             JarOutputStream jarOutput = new JarOutputStream(fileOutput)){
            ZipEntry zedirectory = new ZipEntry("Jarsrc");
            jarOutput.putNextEntry(zedirectory);
            ZipEntry ze = new ZipEntry("Jarsrc/source.java");
            jarOutput.putNextEntry(ze);
            ze = new ZipEntry("Jarsrc/source2.java");
            jarOutput.putNextEntry(ze);
            ze = new ZipEntry("Jarsrc/source3.java");
            jarOutput.putNextEntry(ze);
        }
        return file;
    }

    private void createNestedJar(File innerJar, File outerJar) throws IOException {
        createNestedJar("", innerJar, outerJar);
    }
    private void createNestedJar(String subDirs, File innerJar, File outerJar) throws IOException {
        try (FileInputStream inJarWithManifest = new FileInputStream(innerJar);
             FileOutputStream outJarInJar = new FileOutputStream(outerJar);
             JarOutputStream jaroutJarInJar = new JarOutputStream(outJarInJar)){
            ZipEntry zipE = new ZipEntry(Paths.get(subDirs, innerJar.getName()).toString());
            jaroutJarInJar.putNextEntry(zipE);
            IOUtils.copy(inJarWithManifest, jaroutJarInJar);
            jaroutJarInJar.closeEntry();
        }
    }

    public Path createJarInJar() throws IOException {
        Path jarjarPath = workspace.resolve(jarInJarName);
        File jarInJar = jarjarPath.toFile();
        if(jarInJar.exists()){
            return jarjarPath;
        }

        File jarWithManifest = createJarWithManifest(jarWithManifestName).toFile();

        createNestedJar(jarWithManifest, jarInJar);

        return jarjarPath;
    }

    // creates a Manifest in a Jar in a Jar in a Jar
    public Path createJarInJarInJar() throws IOException {
        jarjarjarPath = workspace.resolve(jarInJarInJarName);
        File jarInJarInJar = jarjarjarPath.toFile();
        if(jarInJarInJar.exists()){
            return jarjarjarPath;
        }

        File jarInJar = createJarInJar().toFile();

        createNestedJar(jarInJar, jarInJarInJar);

        return jarjarjarPath;
    }

    public Path createJarInJarInNestedFolders() throws IOException {
        Path jarjarPath = workspace.resolve(jarInJarName);
        File jarInJar = jarjarPath.toFile();
        if(jarInJar.exists()){
            return jarjarPath;
        }

        File jarWithManifest = createJarWithManifest(jarInFoldersName).toFile();
        String parent = Optional.ofNullable(Paths.get(JarCreator.jarInFoldersName).getParent())
                .orElseThrow(() -> new ExecutionException("The Path [" + jarInFoldersName + "] should have a parent"))
                .toString();
        createNestedJar(parent, jarWithManifest, jarInJar);
        return jarjarPath;
    }
}
