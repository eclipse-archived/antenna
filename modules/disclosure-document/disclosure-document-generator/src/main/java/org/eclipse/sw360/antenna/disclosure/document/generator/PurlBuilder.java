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

import org.apache.commons.lang3.Validate;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURLBuilder;

/**
 * See {@link https://github.com/package-url/purl-spec}
 *
 */
public final class PurlBuilder {

   private PurlBuilder() {

   }

   /**
    * @param artifactCoordinates
    *           (non-null)
    * @return a PURL
    */
   public static String toPurl( ArtifactCoordinates artifactCoordinates ) {
      Validate.isTrue( artifactCoordinates != null, "ArtifactCoordinates must not be null" );

      if ( artifactCoordinates instanceof MavenCoordinates ) {
         return makeMavenCoordinates( (MavenCoordinates) artifactCoordinates );
      } else if ( artifactCoordinates instanceof DotNetCoordinates ) {
         return makeNetCoordinates( (DotNetCoordinates) artifactCoordinates );
      } else if ( artifactCoordinates instanceof JavaScriptCoordinates ) {
         return makeJSCoordinates( (JavaScriptCoordinates) artifactCoordinates );
      } else {
         throw new IllegalStateException( "Coordinates " + artifactCoordinates + " are not supported. You might want to provide an implementation." );
      }

   }

   private static String makeJSCoordinates( JavaScriptCoordinates artifactCoordinates ) {

      PackageURLBuilder aPackageURL = PackageURLBuilder.aPackageURL();
      String aid = artifactCoordinates.getName();
      if ( aid.contains( "/" ) ) {
         String[] arr = aid.split( "/" );
         aPackageURL = aPackageURL.withNamespace( arr[0] );
         aPackageURL = aPackageURL.withName( arr[1] );
      } else {
         aPackageURL = aPackageURL.withName( aid );
      }

      aPackageURL = aPackageURL
            .withType( "npm" )
            .withVersion( artifactCoordinates.getVersion() );
      return build( aPackageURL );
   }

   private static String build( PackageURLBuilder aPackageURL ) {
      try {
         return aPackageURL.build().toString();
      } catch ( MalformedPackageURLException e ) {
         throw new IllegalStateException( e );
      }
   }

   private static String makeNetCoordinates( DotNetCoordinates artifactCoordinates ) {
      PackageURLBuilder aPackageURL = PackageURLBuilder.aPackageURL()
            .withType( "nuget" )
            .withName( artifactCoordinates.getName() )
            .withVersion( artifactCoordinates.getVersion() );
      return build( aPackageURL );
   }

   private static String makeMavenCoordinates( MavenCoordinates artifactCoordinates ) {

      PackageURLBuilder aPackageURL = PackageURLBuilder.aPackageURL()
            .withType( "maven" )
            .withNamespace( artifactCoordinates.getGroupId() )
            .withName( artifactCoordinates.getArtifactId() )
            .withVersion( artifactCoordinates.getVersion() );
      return build( aPackageURL );
   }
}
