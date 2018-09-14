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

package org.eclipse.sw360.antenna.workflow.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.repository.Attachable;

/**
 *
 * Creates a csv file that contains artifact name, version and license.
 *
 * artifactName;version;license;
 */
public class CSVGenerator extends AbstractGenerator {
    public static final String ANTENNA_ARTIFACT_INFORMATION_CSV = "Antenna_artifactInformation.csv";
    public static final String IDENTIFIER = "artifact-information";
    public static final String CLASSIFIER = "antenna-artifact-info";
    public static final String TYPE = "csv";
    private Path targetDirectory;
    private Charset encoding;

    /**
     * Creates a csv file that contains artifact name, version and license. File
     * will be written to CsvFileWriter.ANTENNA_ARTIFACT_INFORMATION_CSV
     */

    private void writeFile(StringBuilder information, File csvFile) {
        try (PrintWriter out = new PrintWriter(csvFile, encoding.toString())) {
            out.write(information.toString());
        }catch (FileNotFoundException e) {
            throw new AntennaExecutionException("The csvFile " + csvFile.getName() + " was not found ", e);
        } catch (UnsupportedEncodingException e){
            throw new AntennaExecutionException("The encoding " + encoding.toString() + " is unsupported ", e);
        }
    }

    private StringBuilder createStringBuilder(Collection<Artifact> artifacts) {
        StringBuilder information = new StringBuilder();
        information.append("artifactName;artifactId;groupId;mavenVersion;bundleVersion;license \n");
        for (Artifact artifact : artifacts) {
            appendInformation(information, artifact.getArtifactIdentifier().getFilename());
            appendInformation(information, artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId());
            appendInformation(information, artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId());
            appendInformation(information, artifact.getArtifactIdentifier().getMavenCoordinates().getVersion());
            appendInformation(information, artifact.getArtifactIdentifier().getBundleCoordinates().getBundleVersion());
            LicenseInformation finalLicenses = artifact.getFinalLicenses();
            StringBuilder licenses = new StringBuilder();
            if (finalLicenses.evaluateLong() != null) {
                licenses.append(finalLicenses.evaluateLong());
                licenses.append(" ");
            } else {
                licenses.append(finalLicenses.evaluate());
                licenses.append(" ");
            }
            information.append(licenses);
            licenses.append(";");
            information.append("\n");
        }
        return information;
    }

    private void appendInformation(StringBuilder sb, String information) {
        Optional.ofNullable(information)
                .ifPresent(sb::append);
        sb.append(";");
    }


    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) {
        StringBuilder information = createStringBuilder(artifacts);
        File csvFile = this.targetDirectory.resolve(ANTENNA_ARTIFACT_INFORMATION_CSV).toFile();
        writeFile(information, csvFile);
        return Collections.singletonMap(IDENTIFIER, new Attachable(TYPE, CLASSIFIER, csvFile));
    }

    @Override
    public void configure(Map<String, String> configMap) {
        this.targetDirectory = context.getToolConfiguration().getAntennaTargetDirectory();
        this.encoding = context.getToolConfiguration().getEncoding();
    }
}