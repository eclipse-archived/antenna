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

package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.eclipse.sw360.antenna.repository.Attachable;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.*;
import org.eclipse.sw360.licenseinfo.outputGenerators.DocxGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.OutputGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.TextGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.XhtmlGenerator;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates a pdf document from the given xsl stylesheet and the xml document.
 */
public class SW360DisclosureDocumentGenerator extends AbstractGenerator {

    private static final String DISCLOSURE_DOC_FORMATS_KEY = "disclosure.doc.formats";
    private static final String DISCLOSURE_DOC_BASEPATH_KEY = "disclosure.doc.basepath";
    private static final String IDENTIFIER_PREFIX = "disclosure-sw360-doc-";
    private static final String CLASSIFIER = "antenna-sw360-disclosure-doc";

    private static final String DOCX_KEY = "docx";
    private static final String TXT_KEY = "txt";
    private static final String HTML_KEY = "html";

    private static final Set<String> outputFormats = Stream.of(DOCX_KEY, TXT_KEY, HTML_KEY).collect(Collectors.toSet());
    private Set<String> selectedOutputFormats;

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        if(configMap.containsKey(DISCLOSURE_DOC_FORMATS_KEY)){
            String selectedOutputFormatsString = getConfigValue(DISCLOSURE_DOC_FORMATS_KEY, configMap);
            selectedOutputFormats = Arrays.stream(selectedOutputFormatsString.split(","))
                    .filter(outputFormats::contains)
                    .collect(Collectors.toSet());
        } else {
            selectedOutputFormats = outputFormats;
        }
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) {
        Collection<LicenseInfoParsingResult> projectLicenseInfoResults = getParsingResults(intermediates);
        Map<String, IAttachable> results = new HashMap<>();

        if(selectedOutputFormats.contains(DOCX_KEY)){
            results.putAll(produce(new DocxGenerator(OutputFormatVariant.DISCLOSURE, ""), projectLicenseInfoResults));
        }
        if(selectedOutputFormats.contains(TXT_KEY)){
            results.putAll(produce(new TextGenerator(OutputFormatVariant.DISCLOSURE, ""), projectLicenseInfoResults));
        }
        if(selectedOutputFormats.contains(HTML_KEY)){
            results.putAll(produce(new XhtmlGenerator(OutputFormatVariant.DISCLOSURE, ""), projectLicenseInfoResults));
        }
        return results;
    }

    private Collection<LicenseInfoParsingResult> getParsingResults(Collection<Artifact> intermediates) {
        return intermediates.stream()
                .map(a -> {
                    final LicenseInfoParsingResult licenseInfoParsingResult = new LicenseInfoParsingResult();

                    final ArtifactIdentifier artifactIdentifier = a.getArtifactIdentifier();
                    final MavenCoordinates mavenCoordinates = artifactIdentifier.getMavenCoordinates();
                    final BundleCoordinates bundleCoordinates = artifactIdentifier.getBundleCoordinates();
                    final JavaScriptCoordinates javaScriptCoordinates = artifactIdentifier.getJavaScriptCoordinates();
                    if(mavenCoordinates != null &&
                            (mavenCoordinates.getVersion() != null || mavenCoordinates.getArtifactId() != null)) {
                        licenseInfoParsingResult.setVersion(mavenCoordinates.getVersion());
                        licenseInfoParsingResult.setName(mavenCoordinates.getGroupId() + ":" + mavenCoordinates.getArtifactId());
                    } else if(bundleCoordinates != null &&
                            (bundleCoordinates.getBundleVersion() != null || bundleCoordinates.getSymbolicName() != null)) {
                        licenseInfoParsingResult.setVersion(bundleCoordinates.getBundleVersion());
                        licenseInfoParsingResult.setName(bundleCoordinates.getSymbolicName());
                    } else if(javaScriptCoordinates != null &&
                            (javaScriptCoordinates.getArtifactId() != null || javaScriptCoordinates.getVersion() != null)) {
                        licenseInfoParsingResult.setVersion(javaScriptCoordinates.getVersion());
                        licenseInfoParsingResult.setName(javaScriptCoordinates.getName());
                    } else {
                        licenseInfoParsingResult.setName(artifactIdentifier.getFilename());
                    }

                    if (licenseInfoParsingResult.getName() == null || "".equals(licenseInfoParsingResult.getName())){
                        licenseInfoParsingResult.setName("UNKNOWN");
                    }
                    if (licenseInfoParsingResult.getVersion() == null || "".equals(licenseInfoParsingResult.getVersion())){
                        licenseInfoParsingResult.setVersion("UNKNOWN");
                    }

                    LicenseInfo licenseInfo = new LicenseInfo();
                    Set<LicenseNameWithText> licenseNamesWithTexts = a.getFinalLicenses()
                            .getLicenses().stream()
                            .map(l -> new LicenseNameWithText()
                                    .setLicenseText(l.getText())
                                    .setLicenseName(l.getName()))
                            .collect(Collectors.toSet());
                    licenseInfo.setLicenseNamesWithTexts(licenseNamesWithTexts);
                    licenseInfoParsingResult.setLicenseInfo(licenseInfo);

                    return licenseInfoParsingResult;
                }).collect(Collectors.toSet());
    }

    private Map<String, IAttachable> produce(OutputGenerator<?> generator, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        LicenseInfoFile licenseInfoFile = getLicenseInfoFile(generator, projectLicenseInfoResults);
        final ByteBuffer generatedOutput = licenseInfoFile.generatedOutput;

        Path outputPath = context.getToolConfiguration().getAntennaTargetDirectory().resolve("AntennaDisclosureDocument."+generator.getOutputType());

        try(FileOutputStream outputStream = new FileOutputStream(outputPath.toString(), false)){
            FileChannel wChannel = outputStream.getChannel();
            wChannel.write(generatedOutput);
        } catch (IOException e) {
            throw new AntennaExecutionException("Failed to write output via SW360 Library", e);
        }

        // TODO: write the file to the archive?
        //ToolConfiguration toolConfig = context.getToolConfiguration();
        //new FileToArchiveWriter().addFileToArchive(toolConfig.getDisclosureDocPathInArchive(),
        //        outputPath);

        return Collections.singletonMap(IDENTIFIER_PREFIX + generator.getOutputType(), new Attachable(generator.getOutputType(), CLASSIFIER, outputPath.toFile()));
    }

    private LicenseInfoFile getLicenseInfoFile(OutputGenerator<?> generator, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {

        LicenseInfoFile licenseInfoFile = new LicenseInfoFile();

        licenseInfoFile.setOutputFormatInfo(generator.getOutputFormatInfo());
        Object output;
        try {
            final ToolConfiguration toolConfiguration = context.getToolConfiguration();
            String projectName = Optional.ofNullable(toolConfiguration.getProductFullName())
                    .orElse(Optional.ofNullable(toolConfiguration.getProductName())
                            .orElse(""));
            String licenseInfoHeaderText = Optional.ofNullable(toolConfiguration.getCopyrightNotice()).orElse("");
            output = generator.generateOutputFile(projectLicenseInfoResults, projectName, "", licenseInfoHeaderText);
        } catch (SW360Exception e) {
            throw new AntennaExecutionException("Failed to generate output via SW360 libraries",e);
        }
        if (output instanceof byte[]) {
            licenseInfoFile.setGeneratedOutput((byte[]) output);
        } else if (output instanceof String) {
            licenseInfoFile.setGeneratedOutput(((String) output).getBytes());
        } else {
            throw new AntennaExecutionException("Unsupported output generator result: " + output.getClass().getName());
        }

        return licenseInfoFile;
    }
}
