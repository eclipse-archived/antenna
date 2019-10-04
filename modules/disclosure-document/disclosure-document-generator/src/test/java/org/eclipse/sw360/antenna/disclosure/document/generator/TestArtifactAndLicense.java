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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.sw360.antenna.disclosure.document.core.ArtifactAndLicense;

public class TestArtifactAndLicense implements ArtifactAndLicense {

   private String filename;
   private List<LicenseInfo> licenses;
   private String copyright;
   private String purl;

   public TestArtifactAndLicense( String filename, String purl ) {
      this( filename, purl, null, null );
   }

   public TestArtifactAndLicense( String filename, String purl, List<LicenseInfo> licenses ) {
      this( filename, purl, licenses, null );
   }

   public TestArtifactAndLicense( String filename, String purl, List<LicenseInfo> licenses, String copyright ) {
      this.filename = filename;
      this.licenses = licenses != null ? licenses : Collections.emptyList();
      this.copyright = copyright;
      this.purl = purl;
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
      return Optional.ofNullable( copyright );
   }

   @Override
   public Optional<String> getPurl() {
      return Optional.ofNullable( purl );
   }

}