/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.testing;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.eclipse.sw360.antenna.api.FrontendCommons;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;

public class SystemTestHelper {

    private static final Path PARTIAL_PDF_PATH = Paths.get("target", "3rdparty-licenses.pdf");
    private static final Path PARTIAL_ZIP_PATH = Paths.get("target", "3rdparty-sources.zip");
    private static final String PARTIAL_HTML_PATH = "3rdparty-licenses.html";
    private static final String PARTIAL_MERGED_WORKFLOW_PATH = "output.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestHelper.class);

    private static ByteArrayOutputStream prepareTest(String expectedResult) throws  IOException {
        InputStream resourceStream = SystemTest.class
                .getResourceAsStream(expectedResult);

        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
        IOUtils.copy(resourceStream, expectedOutput);
        expectedOutput.close();
        return expectedOutput;
    }

    private static boolean checkString(String string, String... regExps) {
        return Arrays.stream(regExps)
                .map(regExp -> Pattern.compile(Pattern.quote(regExp)))
                .map(pattern -> pattern.matcher(string))
                .anyMatch(Matcher::find);
    }

    private static boolean checkPDF(File pdfFile, String... regExps) throws IOException {
        try ( PDDocument pdf = PDDocument.load(pdfFile) ) {
            PDFTextStripper stripper = new PDFTextStripper();
            String textContent = stripper.getText(pdf);
//            if (DEBUG) {
//                try (Writer writer = new FileWriter(pdfFile.getParentFile().toPath().resolve("pdftext").toFile())) {
//                    writer.write(textContent);
//                } catch (IOException e) {
//                    LOGGER.error("Failed to write pdf text content to file");
//                }
//            }
            return checkString(textContent, regExps);
        }
    }

    private static boolean checkHTML_XML(File file, String... regExps) throws IOException{
        String hXFile = FileUtils.readFileToString(file);
        return checkString(hXFile, regExps);
    }

    private static void checkZIP(ZipFile zipFile, String expectedResult, int expectedFiles) {
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        int ind = 0;
        while (zipEntries.hasMoreElements()) {
            ind++;
            ZipEntry entry = zipEntries.nextElement();
            Pattern pattern = Pattern.compile(Pattern.quote(entry.getName()));
            Matcher matcher = pattern.matcher(expectedResult);
            if (!matcher.find()) {
                LOGGER.warn("This file shouldn't exist: "+ entry.getName());
                throw new AssertionError("Found unexpected file.");
            }
        }
        assertEquals("Wrong number of files.", expectedFiles, ind);
    }

    static void pdfTest(Path projectDir, String expectedPdfResult) throws IOException {
        File pdfOutput = projectDir.resolve(PARTIAL_PDF_PATH).toFile();
        Assert.assertTrue(pdfOutput.exists());

        ByteArrayOutputStream expectedPdf = prepareTest(expectedPdfResult);
        Assert.assertTrue(checkPDF(pdfOutput, expectedPdf.toString(), "All rights reserved"));
    }

    static void htmlTest(Path projectDir, String expectedHtmlResult) throws IOException{
        File htmlOutput = projectDir.resolve(Paths.get("target", FrontendCommons.ANTENNA_DIR, PARTIAL_HTML_PATH)).toFile();
        Assert.assertTrue(htmlOutput.exists());

        ByteArrayOutputStream expectedHtml = prepareTest(expectedHtmlResult);
        Assert.assertTrue(checkHTML_XML(htmlOutput, expectedHtml.toString()));
    }

    static void zipTest(Path projectDir, int expectedFiles) throws IOException{
        ZipFile zipOutput = new ZipFile(projectDir.resolve(PARTIAL_ZIP_PATH).toFile());

        ByteArrayOutputStream expectedZip = prepareTest("/analyzer/jsonanalyzer/expected_zip_result");
        checkZIP(zipOutput, expectedZip.toString(), expectedFiles);
    }

    static void workflowTest(Path projectDir, String expectedMergedResult) throws IOException{
        File mergedOutput = projectDir.resolve(Paths.get(PARTIAL_MERGED_WORKFLOW_PATH)).toFile();
        Assert.assertTrue(mergedOutput.exists());

        ByteArrayOutputStream expectedMergedWorkflow = prepareTest(expectedMergedResult);
        Assert.assertTrue(checkHTML_XML(mergedOutput, expectedMergedWorkflow.toString()));
    }
}
