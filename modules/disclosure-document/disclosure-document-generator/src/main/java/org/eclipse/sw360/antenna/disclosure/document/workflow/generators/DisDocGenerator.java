/**
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.disclosure.document.workflow.generators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.sw360.antenna.api.Attachable;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.api.workflow.ProcessingState;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.disclosure.document.core.ArtifactAndLicense;
import org.eclipse.sw360.antenna.disclosure.document.core.DisDoc;
import org.eclipse.sw360.antenna.disclosure.document.core.DocumentValues;

/**
 * Disclosure Document Generator for Antenna.
 * <p />
 * This can be added as a step in the workflow configuration of Antenna. Please consult the Antenna documentation on how
 * to do this.
 */
public class DisDocGenerator extends AbstractGenerator {

   private static final Logger LOG = LoggerFactory.getLogger( DisDocGenerator.class );

   private static final String IDENTIFIER = "antenna-disclosure-doc";
   private static final String CLASSIFIER = "antenna-disclosure-doc";
   private static final String TYPE = "pdf";
   private static final String CONFIG_TEMPLATE_KEY = "disclosure.doc.templateKey";
   private static final String CONFIG_PRODUCT_NAME = "disclosure.doc.productName";
   private static final String CONFIG_PRODUCT_VERSION = "disclosure.doc.productVersion";
   private static final String CONFIG_COPYRIGHT_HOLDER = "disclosure.doc.copyrightHolder";
   private static final String WORKING_DIR_NAME = "disdoc";

   private DocumentValues values;
   private String templateKey;

   @Override
   public Map<String, IAttachable> produce( ProcessingState previousState ) {
      return produce( previousState.getArtifacts() );
   }

   @Override
   public Map<String, IAttachable> produce( Collection<Artifact> artifacts ) {

      long start = System.currentTimeMillis();
      LOG.info( ">>Disclosure Doc<< Starting document generation .." );
      Path antennaDir = context.getToolConfiguration().getAntennaTargetDirectory();
      File ossDir = createWorkDir( antennaDir );

      DisDoc disDoc = new DisDoc( ossDir, templateKey, values );

      File disclosureDoc = disDoc.generate( convertAndSort( artifacts ) );
      long duration = System.currentTimeMillis() - start;
      LOG.info( ">>Disclosure Doc<< finshed in {}ms", duration );

      return Collections.singletonMap( IDENTIFIER, new Attachable( TYPE, CLASSIFIER, disclosureDoc ) );
   }

   private List<ArtifactAndLicense> convertAndSort( Collection<Artifact> artifacts ) {
      List<ArtifactAndLicense> list = artifacts.stream()
            .filter( artifact -> !artifact.isProprietary() )
            .map( ArtifactAdapter::new ).collect( Collectors.toList() );
      Collections.sort( list, new ArtifactAndLicenseComparator() );
      return list;
   }

   @Override
   public void configure( Map<String, String> configMap ) {

      String templateKeyConfig = getConfigValue(CONFIG_TEMPLATE_KEY, configMap );
      Validate.isTrue( StringUtils.isNotBlank( templateKeyConfig ),
            "You must set a value for " + CONFIG_TEMPLATE_KEY + " (was >" + templateKeyConfig + "<)" );
      this.templateKey = templateKeyConfig;

      String confProductName = getConfigValue(CONFIG_PRODUCT_NAME, configMap );
      String confProductVersion = getConfigValue(CONFIG_PRODUCT_VERSION, configMap );
      String confCopyrightHolder = getConfigValue(CONFIG_COPYRIGHT_HOLDER, configMap );

      this.values = new DocumentValues( confProductName, confProductVersion, confCopyrightHolder );
   }

   private File createWorkDir(Path antennaDir ) {
      File workDir = new File( antennaDir.toFile(), WORKING_DIR_NAME);
      try {
         FileUtils.forceMkdir( workDir );
         return workDir;
      } catch ( IOException e ) {
         throw new ConfigurationException( "Unable to create working directory in path " + workDir, e );
      }
   }

}
