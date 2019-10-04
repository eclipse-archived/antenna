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
package org.eclipse.sw360.antenna.disclosure.document.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;

import org.eclipse.sw360.antenna.disclosure.document.core.ArtifactAndLicense;

/**
 * This adapter helps to encapsulate Antenna API.
 * <p />
 * In the current state it may still be changed and this should help to limit the places that need to change to < 2.
 */
public class ArtifactAdapter implements ArtifactAndLicense {

   private final String filename;
   private final Optional<String> copyrightStatement;
   private final List<LicenseInfo> licenses;
   private final String purl;

   @SuppressWarnings( "rawtypes" )
   private static final List<Class<? extends ArtifactCoordinates>> SUPPORTED_COORDINATION_TYPES = Collections.unmodifiableList( Arrays.asList(
         MavenCoordinates.class,
         JavaScriptCoordinates.class,
         DotNetCoordinates.class ) );

   /**
    * @param artifact
    *           (non-null) The Antenna artifact to get information from.
    */
   public ArtifactAdapter( Artifact artifact ) {
      filename = figureOutFileName( artifact );
      copyrightStatement = artifact.askForGet( CopyrightStatement.class );
      LicenseInformation licenseInformation = ArtifactLicenseUtils.getFinalLicenses( artifact );
      this.licenses = createLicenses( licenseInformation );
      this.purl = toPurl( artifact );
   }

   private static String figureOutFileName( Artifact artifact ) {
      final String filename;
      Optional<ArtifactFilename> optional = artifact.askFor( ArtifactFilename.class );
      if ( optional.isPresent() ) {
         filename = StringUtils.join( optional.get().getFilenames(), ',' );
      } else {
         filename = tryYourLuckWithPathnames( artifact );
      }
      return filename;
   }

   private static String tryYourLuckWithPathnames( Artifact artifact ) {

      Optional<List<String>> optional = artifact.askForGet( ArtifactPathnames.class );
      if ( optional.isPresent() ) {
         List<String> list = optional.get();
         Set<String> collected = list.stream().map( s -> FilenameUtils.getName( s ) ).collect( Collectors.toSet() );
         return StringUtils.join( collected, ',' );
      }
      return "";
   }

   private static List<LicenseInfo> createLicenses( LicenseInformation licenseInformation ) {
      List<License> allLicenses = licenseInformation.getLicenses();
      List<LicenseInfo> list = new ArrayList<>( allLicenses.size() );

      for ( License license : allLicenses ) {
         list.add( new LicenseInfo( createKey( license ), getLicenseText( license ), license.getName(), license.getLongName() ) );
      }
      return list;
   }

   /**
    * Makes sure, a useable license text is there.
    *
    * @param license
    *           (non-null)
    * @return (non-null) A license text or according placeholder.
    */
   public static String getLicenseText( License license ) {
      String text = license.getText();
      text = text != null ? text : "No license text available";
      // remove thie magic unicode as it will likely not be in a unicode font set.
      return StringUtils.replace( text, "\uFEFF", "" );
   }

   /**
    * Creates a key from license information that is unique and can be used as anchor.
    * 
    * @param license
    *           (non-null)
    * @return (non-null)
    */
   public static String createKey( License license ) {
      // valid keys are likely: numbers and letters but no special chars or even '-'
      int hashCode = license.getName().hashCode();
      String prefix = hashCode < 0 ? "n" : "p";
      return prefix + Math.abs( hashCode );
   }

   private String toPurl( Artifact artifact ) {
      @SuppressWarnings( "rawtypes" )
      ArtifactCoordinates artifactCoordinates = figureOutCoordinates( artifact );

      if ( artifactCoordinates != null ) {
         return PurlBuilder.toPurl( artifactCoordinates );
      }

      return null;
   }

   @SuppressWarnings( "rawtypes" )
   private static ArtifactCoordinates figureOutCoordinates( Artifact artifact ) {

      for ( Class<? extends ArtifactCoordinates> clazz : SUPPORTED_COORDINATION_TYPES ) {
         Optional<? extends ArtifactCoordinates> optional = artifact.askFor( clazz );
         if ( optional.isPresent() ) {
            return optional.get();
         }
      }
      return null;
   }

   @Override
   public String getFilename() {
      return filename;
   }

   @Override
   public List<LicenseInfo> getLicenses() {
      return licenses;
   }

   @Override
   public Optional<String> getCopyrightStatement() {
      return copyrightStatement;
   }

   @Override
   public Optional<String> getPurl() {
      return Optional.ofNullable( purl );
   }

}
