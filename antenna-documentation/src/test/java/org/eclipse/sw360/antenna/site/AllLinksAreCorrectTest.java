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

package org.eclipse.sw360.antenna.site;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AllLinksAreCorrectTest {
    private static final String ANTENNA_DOCUMENTATION = "src" + File.separator + "site";
    private static final String GENERATED_SOURCES = "target" + File.separator + "generated-sources";
    private static final String MARKDOWN = "markdown";
    private static final Pattern LINK_PATTERN = Pattern.compile("(\\[.*\\])(\\(((?!https).*(\\.html|\\.md)#?[^)]*)\\))");
    private static final Pattern ADDITIONAL_LINK_PATTERN = Pattern.compile("(\\[.*\\]:\\s)((?!https).*(\\.html|\\.md)#?.*)");

    @Test
    public void testLinks() throws IOException {
        List<Path> allFiles = findAllFiles();
        List<String> expectedResolvedHyperlinks = mapFilesToRelativePathsAsInLinks(allFiles);
        for (Path file : allFiles) {
            List<String> sanitizedHyperlinks = sanitizeHyperlinks(extractHyperlinks(file));
            for (String hyperlink : sanitizedHyperlinks) {
                assertThat(expectedResolvedHyperlinks).as("Checking link %s in file %s", hyperlink, file).contains(resolveHyperlinkPath(file, hyperlink));
            }
        }
    }

    @Test
    public void testPatterns() {
        assertThat(LINK_PATTERN.matcher("test [link](./some/path.html) test").find()).isTrue();
        assertThat(LINK_PATTERN.matcher("test [link](./some/path.html#bla) test").find()).isTrue();
        assertThat(LINK_PATTERN.matcher("test [link](https://some/url.html) test").find()).isFalse();
        assertThat(LINK_PATTERN.matcher("test [link](./some/path.md) test").find()).isTrue();  // we need to find md files because those pose a common
        Matcher bracesFinder = LINK_PATTERN.matcher("test ([link](./some/path.html)) test");  // sometimes links can be within braces
        assertThat(bracesFinder.find()).isTrue();
        assertThat(bracesFinder.group(3)).isEqualTo("./some/path.html");
        assertThat(ADDITIONAL_LINK_PATTERN.matcher("[link]: ./some/url.html").find()).isTrue();
        assertThat(ADDITIONAL_LINK_PATTERN.matcher("[link]: external/${docName}/url.html").find()).isTrue();
        assertThat(ADDITIONAL_LINK_PATTERN.matcher("[link] external/${docName}/url.html").find()).isFalse();
    }

    private List<Path> findAllFiles() throws IOException {
        List<Path> documentationFiles = findFilesInFolder(Paths.get(ANTENNA_DOCUMENTATION, MARKDOWN).toAbsolutePath().normalize());
        List<Path> copiedFiles = findFilesInFolder(Paths.get(GENERATED_SOURCES, MARKDOWN).toAbsolutePath().normalize());
        return Stream.of(documentationFiles, copiedFiles)
                .map(List::stream)
                .flatMap(path -> path)
                .collect(Collectors.toList());
    }

    private List<Path> findFilesInFolder(Path folder) throws IOException {
        return Files.walk(folder)
                .map(Path::toFile)
                .filter(file -> !file.isDirectory())
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    private List<String> mapFilesToRelativePathsAsInLinks(List<Path> files) {
        return files.stream()
                .map(Path::toString)
                .map(path -> path.split(MARKDOWN + "[/\\\\]")[1]) // this gives us the correct hierarchy.
                .map(path -> path.replace(".md", ""))
                .map(path -> path.replace(".vm", ""))
                .collect(Collectors.toList());
    }

    private List<String> extractHyperlinks(Path file) throws IOException {
        List<String> strings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toString()))) {
            String line = reader.readLine();
            while (line != null) {
                Matcher matcher = LINK_PATTERN.matcher(line);
                while (matcher.find()) {
                    strings.add(matcher.group(3).split("#")[0]);
                }
                Matcher additionalMatcher = ADDITIONAL_LINK_PATTERN.matcher(line);
                while (additionalMatcher.find()) {
                    strings.add(additionalMatcher.group(2).split("#")[0]);
                }
                line = reader.readLine();
            }
            return strings;
        }
    }

    private List<String> sanitizeHyperlinks(List<String> hyperlinks) {
        return hyperlinks.stream()
                .map(hyperlink -> hyperlink.replace("${docName}", "antenna"))
                .map(hyperlink -> hyperlink.replace(".html", ""))
                .collect(Collectors.toList());
    }

    private String resolveHyperlinkPath(Path file, String hyperlink) {
        String relativePathFromMarkdownFolder = file.toString().split(MARKDOWN + File.separator)[1];
        String relativePathAtBeginning = relativePathFromMarkdownFolder.lastIndexOf(File.separator) != -1
                ? relativePathFromMarkdownFolder.substring(0, relativePathFromMarkdownFolder.lastIndexOf(File.separator))
                : "";
        return Paths.get(relativePathAtBeginning, hyperlink.replace(".html", ""))
                .normalize()
                .toString();
    }

}
