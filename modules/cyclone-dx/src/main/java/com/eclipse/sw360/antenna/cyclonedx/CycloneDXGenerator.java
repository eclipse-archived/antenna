/*
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2020.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.eclipse.sw360.antenna.cyclonedx;

import org.cyclonedx.BomGenerator;
import org.cyclonedx.BomGeneratorFactory;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.eclipse.sw360.antenna.api.Attachable;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates a CycloneDX Bill-of-Material file with information from Antenna.
 */
public class CycloneDXGenerator extends AbstractGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(CycloneDXGenerator.class);

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) throws ExecutionException {
        LOG.debug("Created a cyclone-dx bill-of-material for {} artifacts", artifacts.size());
        List<Component> components = toComponents(artifacts);
        Bom bom = new Bom();
        bom.setComponents(components);
        BomGenerator gen = BomGeneratorFactory.create(CycloneDxSchema.Version.VERSION_11, bom);
        File targetFile = createTargetFile();
        doGenerate(gen, targetFile);
        LOG.debug("Bill-of-material created in {}", targetFile);
        return Collections.singletonMap("cyclonedx-bom", new Attachable("xml", "cyclonedx-bom", targetFile));
    }

    protected File createTargetFile() {
        ToolConfiguration toolConfig = context.getToolConfiguration();
        Path targetDirectory = toolConfig.getAntennaTargetDirectory();
        return new File(targetDirectory.toFile(), "cyclonedx.bom.xml");
    }

    private void doGenerate(BomGenerator gen, File targetFile)
            throws ExecutionException {
        try {
            Document document = gen.generate();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            DOMSource source = new DOMSource(document);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), UTF_8)) {
                StreamResult result = new StreamResult(writer);
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(source, result);
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new ExecutionException("Unable to generate CycloneDX bom", e);
        }
    }

    private List<Component> toComponents(Collection<Artifact> artifacts) {
        LinkedHashSet<Component> components = new LinkedHashSet<>();
        for (Artifact artifact : artifacts) {
            components.add(ArtifactToComponentConverter.toComponent(artifact));
        }
        return new ArrayList<>(components);
    }
}
