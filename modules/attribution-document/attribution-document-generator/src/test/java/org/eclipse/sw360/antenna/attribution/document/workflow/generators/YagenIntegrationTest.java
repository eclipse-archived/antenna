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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sw360.antenna.attribution.document.core.AttributionDocumentGeneratorImpl;
import org.eclipse.sw360.antenna.attribution.document.core.model.LicenseInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.eclipse.sw360.antenna.attribution.document.core.model.ArtifactAndLicense;
import org.eclipse.sw360.antenna.attribution.document.core.DocumentValues;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is less a good test as there are not much assertions helping here. But it helps during development, as you
 * more or less get everything run through once. Using a breakpoint at the assertion you should find a complete document
 * in the tempdir.
 */
class YagenIntegrationTest {

   @TempDir
   File tmpDir;

   @Test
   void doGenerate() {
      String productName = "My Awesome Product with a very long name, that also contains brackets and stuff";
      String version = "abc123";
      String copyrightHolder = "My Legal Entity GmbH";

      DocumentValues values = new DocumentValues(productName, version, copyrightHolder);

      AttributionDocumentGeneratorImpl attrDocGen = new AttributionDocumentGeneratorImpl("attribution_document.pdf", tmpDir, "antenna-demo", values);

      List<ArtifactAndLicense> foo = createData();
      File file = attrDocGen.generate(foo);
      assertThat(file).exists().hasExtension("pdf");
   }

   private static List<ArtifactAndLicense> createData() {
      List<ArtifactAndLicense> list = new ArrayList<>();

      List<LicenseInfo> aslOnlyLicense = Collections.singletonList(new LicenseInfo("ASL20", "LorumIpsum", "Apache2.0", "Apache License 2.0"));
      list.add(new TestArtifactAndLicense("/aPathToAJar/a.jar", "a:b:123",
            aslOnlyLicense));

      List<LicenseInfo> licenses = new ArrayList<>();
      licenses.add(new LicenseInfo("AGPL", "LorumIpsum", "AGPL", "AGPL"));
      licenses.add(new LicenseInfo("ASL20", "LorumIpsum", "Apache2.0", "Apache License 2.0"));

      list.add(new TestArtifactAndLicense("/some/jar", "pkg:bom/foobar@334", licenses, "a copyright"));

      list.add(new TestArtifactAndLicense("tmp/i-have-no-coordinates.zip", null,
            aslOnlyLicense));

      list.add(new TestArtifactAndLicense("tmp/i-have-a-copyright.zip", null,
            aslOnlyLicense, "Copyright by Chet"));

      list.add(new TestArtifactAndLicense("somepath/somefile.exe", null,
            Collections
                  .singletonList(new LicenseInfo("Beerware", "You know the deal, sponsor a beer if you are using this.", "AGPL", "Beerware License"))));

      for (int i = 0; i < 23; i++) {
         list.add(new TestArtifactAndLicense("tmp/file_" + i + ".zip", "pkg:maven/com.pany/thing@" + i, aslOnlyLicense, null));
      }

      return list;
   }
}
