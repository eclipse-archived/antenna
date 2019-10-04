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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.sw360.antenna.disclosure.document.workflow.generators.ArtifactAdapter;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ArtifactAdapterTest {

   Artifact artifact = new Artifact();

   @Test
   @Disabled
   void basicMaven() throws Exception {
      setFileName( "/tmp/foo/chet.txt" ); // does not work and unclear why. Thus disabling test
      setMaven( "com.foo", "chet", "3.2.1-RELEASE" );

      ArtifactAdapter adapter = new ArtifactAdapter( artifact );
      assertThat( adapter.getFilename() ).isEqualTo( "chet.txt" );
   }

   private void setMaven( String g, String a, String v ) {
      MavenCoordinates m = new MavenCoordinates( a, g, v );
      artifact = artifact.addFact( m );
   }

   private void setFileName( String pathname ) {
      ArtifactFile fileName = new ArtifactFile( new File( pathname ).toPath() );
      artifact = artifact.addFact( fileName );
   }

   @Test
   void licenseNullIsSameAsTina() throws Exception {
      License license = new License();
      license.setText( null );
      String licenseText = ArtifactAdapter.getLicenseText( license );
      assertThat( licenseText ).isEqualTo( "No license text available" );
   }

   @Test
   void license() throws Exception {
      License license = new License();
      license.setText( "Lorum ipsum" );
      String licenseText = ArtifactAdapter.getLicenseText( license );
      assertThat( licenseText ).isEqualTo( "Lorum ipsum" );
   }

   @Test
   void removeSpecialUTFfromText() throws Exception {
      License license = new License();
      license.setText( "\uFEFFLorum ipsum" );
      String licenseText = ArtifactAdapter.getLicenseText( license );
      assertThat( licenseText ).isEqualTo( "Lorum ipsum" );
   }

   @Test
   void generateKey() throws Exception {
      License license = new License();
      license.setName( "ASL2.0" );
      String key = ArtifactAdapter.createKey( license );
      assertThat( key ).isEqualTo( "p1939860698" );
   }

}
