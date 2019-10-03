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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

public class AntennaUtils {

    private static final String ARCHIVE_FILE_EXTENSIONS = "zip|war|jar|ear";

    private AntennaUtils() {
        // Utility class
    }

    /*
     * takes a URL like:
     *    "/some/path/to/file.jar/sub/path/child.war/subChild.jar"
     * and returns an iterator yielding:
     *    - "/some/path/to/file.jar"
     *    - "/sub/path/child.war"
     *    - "/subChild.jar"
     */
    public static Iterator<Path> getJarPathIteratorFromPath(Path jarPath){
        if (!jarPath.isAbsolute()){
            throw new ExecutionException("This function should only be called with absolute paths");
        }
        return new TillJarGroupingIterator(jarPath);
    }

    /*
     * takes a URL like:
     *     "file:/some/path/to/file.jar/child.war/subChild.jar"
     *  or "jar:file:/some/path/to/file.jar!/some/inner/thing.class"
     * and returns:
     *     "/some/path/to/file.jar"
     */
    public static Path getJarPath(URL url) throws URISyntaxException, MalformedURLException {
        if("jar".equals(url.getProtocol())) {
            String file = url.getFile();
            if(file.contains("!")){
                file = file.split("!",2)[0];
            }
            url = new URL(file);
        }
        Path jarPath = Paths.get(url.toURI());
        return getJarPath(jarPath);
    }

    public static Path getJarPath(Path jarPath) {
        Iterator<Path> jarPathIterator = getJarPathIteratorFromPath(jarPath);
        if(! jarPathIterator.hasNext()){
            throw new ExecutionException("Failed to parse jar path");
        }
        return jarPathIterator.next();
    }

    public static String replaceDotInJarExtension(String path) {
        return path.replaceAll("\\.(?=("+AntennaUtils.ARCHIVE_FILE_EXTENSIONS+")$)", "_");
    }

    /*
     * takes a path like:
     *    "/some/path/to/file.jar/child.war/subChild.jar"
     * and returns:
     *     "/some/path/to/file_jar/child_war/subChild.jar"
     */
    public static Path computeInnerReplacementJarPath(Path jarPath){
        Iterator<Path> jarPathIterator = getJarPathIteratorFromPath(jarPath);
        if(!jarPathIterator.hasNext()){
            throw new ExecutionException("Tried to computeInnerReplacementPath from broken Path");
        }
        Path cleanedUp = jarPathIterator.next();
        while(jarPathIterator.hasNext()){
            final String basename = Optional.ofNullable(cleanedUp.getFileName())
                    .orElseThrow(() -> new ExecutionException("Error in computeInnerReplacementJarPath"))
                    .toString();
            final String parent = Optional.ofNullable(cleanedUp.getParent())
                    .orElseThrow(() -> new ExecutionException("Error in computeInnerReplacementJarPath"))
                    .toString();

            String cleanedUpBasename = replaceDotInJarExtension(basename);
            cleanedUp = Paths.get(parent, cleanedUpBasename, jarPathIterator.next().toString());
        }
        return cleanedUp;
    }

    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private static class TillJarGroupingIterator implements Iterator<Path> {
        private Iterator<Path> pathIterator;
        private Path currentRoot;
        private final Path root = Paths.get("/");

        TillJarGroupingIterator(Path path) {
            pathIterator = path.toAbsolutePath().iterator();
            currentRoot = path.getRoot();
            if(currentRoot == null) {
                currentRoot = Paths.get(".");
            }
        }

        @Override
        public boolean hasNext() {
            return pathIterator.hasNext();
        }

        private boolean isNotAnArchive(Path pathEntry) {
            return ! pathEntry.toString().matches(".*\\.("+AntennaUtils.ARCHIVE_FILE_EXTENSIONS+")$");
        }

        @Override
        public Path next() {
            Path next = currentRoot;

            while(pathIterator.hasNext() && isNotAnArchive(next)){
                next = next.resolve(pathIterator.next());
            }

            currentRoot = root;
            return next;
        }
    }
}
