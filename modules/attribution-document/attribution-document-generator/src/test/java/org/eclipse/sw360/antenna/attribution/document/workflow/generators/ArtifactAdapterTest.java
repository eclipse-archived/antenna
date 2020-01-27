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
package org.eclipse.sw360.antenna.attribution.document.workflow.generators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import org.junit.jupiter.api.Test;

class ArtifactAdapterTest {
   Artifact artifact = new Artifact()
                           .addFact(new ArtifactPathnames("/tmp/foo/chet.txt"))
                           .addFact(new ArtifactCoordinates(new Coordinate(Coordinate.Types.MAVEN, "com.foo", "chet", "3.2.1-RELEASE")));
   Artifact artifact2 = new Artifact()
                           .addFact(new ArtifactFilename("artifactFilename1.jar"))
                           .addFact(new ArtifactPathnames("sub/dir/anotherFilename2.jar"))
                           .addFact(new ArtifactCoordinates(new Coordinate(Coordinate.Types.MAVEN, "org.example", "any-artifact", "1.0.0")))
                           .addFact(new CopyrightStatement("Some lines of copyright statement."))
                           .addFact(new DeclaredLicenseInformation(LicenseSupport.mapLicenses(Arrays.asList("License-ID"))));

   @Test
   public void complexMavenArtifactTest() {
      ArtifactAdapter adapterArtifact = new ArtifactAdapter(artifact2);

      assertThat(adapterArtifact.getFilename()).isEqualTo("artifactFilename1.jar");
      assertThat(adapterArtifact.getFilename()).isEqualTo("anotherFilename2.jar");
      assertThat(adapterArtifact.getCopyrightStatement().get()).isEqualTo("Some lines of copyright statement.");


      assertThat(adapterArtifact.getLicenses().get(0).getKey()).isEqualTo("License-ID");
   }

   @Test
   void basicMavenArtifactTest() {
      ArtifactAdapter adapter = new ArtifactAdapter(artifact);
      assertThat(adapter.getFilename()).isEqualTo("chet.txt");
   }

   @Test
   void licenseWithNullTextTest() {
      License license = new License();
      license.setText(null);
      String licenseText = ArtifactAdapter.getLicenseText(license);
      assertThat(licenseText).isEqualTo("No license text available");
   }

   @Test
   void basicLicenseTest() {
      License license = new License();
      license.setText("Lorum ipsum");
      String licenseText = ArtifactAdapter.getLicenseText(license);
      assertThat(licenseText).isEqualTo("Lorum ipsum");
   }

   @Test
   void removeSpecialUTFfromText() {
      License license = new License();
      license.setText("\uFEFFLorum ipsum");
      String licenseText = ArtifactAdapter.getLicenseText(license);
      assertThat(licenseText).isEqualTo("Lorum ipsum");
   }

   @Test
   void generateKey() {
      License license = new License();
      license.setName("ASL2.0");
      String key = ArtifactAdapter.createKey(license);
      assertThat(key).isEqualTo("p1939860698");
   }
}
