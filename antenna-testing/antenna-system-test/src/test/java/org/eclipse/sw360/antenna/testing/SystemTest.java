/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.testing;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProject;
import org.eclipse.sw360.antenna.frontend.testProjects.FullTestProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.eclipse.sw360.antenna.testing.SystemTestHelper.*;

@RunWith(Parameterized.class)
public class SystemTest {

    private static final boolean KEEPTESTFILES = (System.getProperty("keepTempFiles") != null);

    private AbstractTestProject testProject;
    private AbstractSystemTestRunner systemTestRunner;

    @Parameterized.Parameters(name = "{index}: Test runner = {1}")
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(new Object[][] {
                {new CliSystemTestRunner(), "Cli"},
                {new MavenSystemTestRunner(false), "Maven"}
        });
    }

    public SystemTest(AbstractSystemTestRunner systemTestRunner, String name){
        this.systemTestRunner = systemTestRunner;
    }

    private void initiateAndCopyProject(String testName)
            throws MavenInvocationException, IOException, InterruptedException, URISyntaxException {

        testProject = new FullTestProject();
    }

    private void initiateCopyAndRunProject(String testName)
            throws MavenInvocationException, IOException, InterruptedException, URISyntaxException {
        initiateAndCopyProject(testName);
        int result = systemTestRunner.run(testProject);
        Assert.assertEquals(0, result);
    }

    @After
    public void cleanUp() throws IOException {
        if (!KEEPTESTFILES) {
            testProject.cleanUpTemporaryProjectFolder();
        }
    }

    @Test
    public void testJsonAnalyzer()
            throws Exception {
        initiateCopyAndRunProject("jsonanalyzer");
        testProject.addAndOverwriteFile(SystemTest.class.getResourceAsStream("/analyzer/jsonanalyzer/customWorkflow.xml"), "customWorkflow.xml");
        testProject.addAndOverwriteFile(SystemTest.class.getResourceAsStream("/analyzer/jsonanalyzer/ClmReportData.json"), "ClmReportData.json");
        Path projectDir = testProject.getProjectRoot();

        // PDF Test
        String expectedPdfResult = "/analyzer/jsonanalyzer/expected_result";
        pdfTest(projectDir, expectedPdfResult);

        // HTML Test
        String expectedHtmlResult = "/analyzer/jsonanalyzer/expected_html_result.html";
        htmlTest(projectDir, expectedHtmlResult);

        // ZipFile Test
        int expectedFiles = 799;
        zipTest(projectDir, expectedFiles);

        // workflow Test
        String expectedMergedResult = "/analyzer/jsonanalyzer/expectedMergedWorkflow.xml";
        workflowTest(projectDir, expectedMergedResult);
    }

    @Test
    public void testCsvAnalyzer()
            throws Exception {
        initiateCopyAndRunProject("csvanalyzer");
        testProject.addAndOverwriteFile(SystemTest.class.getResourceAsStream("/analyzer/csvanalyzer/customWorkflow.xml"), "customWorkflow.xml");
        testProject.addAndOverwriteFile(SystemTest.class.getResourceAsStream("/analyzer/csvanalyzer/ClmReportData.json"), "reportdata.csv");
        Path projectDir = testProject.getProjectRoot();

        // PDF Test
        String expectedPdfResult = "/analyzer/jsonanalyzer/expected_result";
        pdfTest(projectDir, expectedPdfResult);

        // HTML Test
        String expectedHtmlResult = "/analyzer/jsonanalyzer/expected_html_result.html";
        htmlTest(projectDir, expectedHtmlResult);

        // ZipFile Test
        int expectedFiles = 799;
        zipTest(projectDir, expectedFiles);

        // workflow Test
        String expectedMergedResult = "/analyzer/csvanalyzer/expectedMergedWorkflow.xml";
        workflowTest(projectDir, expectedMergedResult);
    }
}
