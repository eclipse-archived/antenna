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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.repository.Attachable;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.License;

/**
 * Generates an HTML report of component license information.
 */
public class HTMLReportGenerator extends AbstractGenerator {
    private static final String IDENTIFIER = "disclosure-doc";
    private static final String CLASSIFIER = "antenna-disclosure-doc";
    private static final String TYPE = "html";

    private final String LICENSE_REPORT_TEMPLATE_FILE = "licenseReport.vm";
    private final String LICENSE_REPORT_FILE = "3rdparty-licenses.html";
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLReportGenerator.class);
    private Charset encoding;

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) throws AntennaExecutionException {
        LOGGER.info("Generating HTML report.");

        // Get path to disclosure documents
        ToolConfiguration toolConfig = context.getToolConfiguration();
        Path reportFilePath = toolConfig.getAntennaTargetDirectory().resolve(LICENSE_REPORT_FILE);


        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.NullLogSystem");
        ve.setProperty("resource.loader", "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        // Initialise Velocity and put the artifacts and licenses into context
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("artifacts", artifacts);
        velocityContext.put("licenses", getAllLicenses(artifacts));

        // Write the template to the report file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(reportFilePath.toFile()), encoding)) {
            Template template = ve.getTemplate(LICENSE_REPORT_TEMPLATE_FILE, "utf-8");
            template.merge(velocityContext, writer);
        } catch (IOException e) {
            throw new AntennaExecutionException("Cannot write HTML report file: " + e.getMessage());
        }

        return Collections.singletonMap(IDENTIFIER, new Attachable(TYPE, CLASSIFIER, reportFilePath.toFile()));
    }

    private Set<License> getAllLicenses(Collection<Artifact> artifacts) {
         Set<License> licenses = new HashSet<>();
         artifacts.stream().map(artifact -> artifact.getFinalLicenses().getLicenses())
                 .flatMap(Collection::stream)
                 .forEach(licenses::add);
         return licenses;
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        this.encoding = context.getToolConfiguration().getEncoding();
    }
}
