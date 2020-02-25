/**
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.attribution.document.workflow.generators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sw360.antenna.api.Attachable;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.api.workflow.ProcessingState;
import org.eclipse.sw360.antenna.attribution.document.core.AttributionDocumentGeneratorImpl;
import org.eclipse.sw360.antenna.attribution.document.core.DocumentValues;
import org.eclipse.sw360.antenna.attribution.document.core.model.ArtifactAndLicense;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribution Document Generator for Antenna.
 * <p>
 * This can be added as a step in the workflow configuration of Antenna. Please consult the Antenna documentation on how
 * to do this.
 */
public class AttributionDocumentGenerator extends AbstractGenerator {
   private static final Logger LOG = LoggerFactory.getLogger(AttributionDocumentGenerator.class);
   private static final String DEFAULT_ATTRIBUTION_DOC_NAME = "attribution-document.pdf";
   private static final String DEFAULT_TEMPLATE_KEY = "basic-pdf-template";

   private static final String IDENTIFIER = "antenna-attribution-doc";
   private static final String CLASSIFIER = "antenna-attribution-doc";
   private static final String TYPE = "pdf";
   private static final String CONFIG_DOC_NAME = "attribution.doc.name";
   private static final String CONFIG_TEMPLATE_KEY = "attribution.doc.templateKey";
   private static final String CONFIG_PRODUCT_NAME = "attribution.doc.productName";
   private static final String CONFIG_PRODUCT_VERSION = "attribution.doc.productVersion";
   private static final String CONFIG_COPYRIGHT_HOLDER = "attribution.doc.copyrightHolder";
   private static final String WORKING_DIR_NAME = "doc-gen";

   private DocumentValues values;
   private String templateKey;
   private String docName;

   private File docGenDir;

   public AttributionDocumentGenerator() {
      this.workflowStepOrder = 700;
   }


   @Override
   public Map<String, IAttachable> produce(ProcessingState previousState) {
      return produce(previousState.getArtifacts());
   }

   @Override
   public Map<String, IAttachable> produce(Collection<Artifact> artifacts) {
      long start = System.currentTimeMillis();
      LOG.info("Generating pdf report.");
      Path antennaDir = context.getToolConfiguration().getAntennaTargetDirectory();
      docGenDir = createWorkDir(antennaDir);

      AttributionDocumentGeneratorImpl disDoc = new AttributionDocumentGeneratorImpl(docName, docGenDir, templateKey, values);

      File attributionDoc = disDoc.generate(convertAndSort(artifacts));
      long duration = System.currentTimeMillis() - start;
      LOG.debug("Generating pdf report finished in {}ms.", duration);

      return Collections.singletonMap(IDENTIFIER, new Attachable(TYPE, CLASSIFIER, attributionDoc));
   }

   private List<ArtifactAndLicense> convertAndSort(Collection<Artifact> artifacts) {
      List<ArtifactAndLicense> list = artifacts.stream()
            .filter(artifact -> !artifact.isProprietary())
            .map(ArtifactAdapter::new).collect(Collectors.toList());
      Collections.sort(list, new ArtifactAndLicenseComparator());
      return list;
   }

   @Override
   public void configure(Map<String, String> configMap) {
      final String confProductName = getConfigValue(CONFIG_PRODUCT_NAME, configMap, context.getToolConfiguration().getProductFullName());
      final String confProductVersion = getConfigValue(CONFIG_PRODUCT_VERSION, configMap, context.getToolConfiguration().getVersion());
      final String confCopyrightHolder = getConfigValue(CONFIG_COPYRIGHT_HOLDER, configMap);

      docName = getConfigValue(CONFIG_DOC_NAME, configMap, DEFAULT_ATTRIBUTION_DOC_NAME);
      templateKey = getConfigValue(CONFIG_TEMPLATE_KEY, configMap, DEFAULT_TEMPLATE_KEY);

      values = new DocumentValues(confProductName, confProductVersion, confCopyrightHolder);
   }

   private File createWorkDir(Path antennaDir) {
      File workDir = new File(antennaDir.toFile(), WORKING_DIR_NAME);
      if (!workDir.mkdirs()) {
         throw new ConfigurationException("Unable to create working directory in path " + workDir);
      }
      return workDir;
   }

   @Override
   public void cleanup() {
      if (docGenDir != null) {
         Path cleanUpDir = docGenDir.toPath();

         if (!context.getDebug()) {
            try {
               Files.walk(cleanUpDir)
                       .filter(p -> ! p.getFileName().equals(Paths.get(docName)))
                       .map(Path::toFile)
                       .forEach(File::delete);
            } catch (IOException e) {
               LOG.debug("Failed to clean up temporary directory=[" + docGenDir +
                       "] for the generation of the attribution document");
            }
         }
      }
   }
}
