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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.sw360.antenna.attribution.document.core.model.ArtifactAndLicense;
import org.eclipse.sw360.antenna.attribution.document.core.model.LicenseInfo;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;

/**
 * This adapter helps to encapsulate Antenna API.
 * <p>
 * In the current state it may still be changed and this should help to limit the places that need to change to
 * {@literal <} 2.
 */
public class ArtifactAdapter implements ArtifactAndLicense {

   private final String filename;
   private final Optional<String> copyrightStatement;
   private final List<LicenseInfo> licenses;
   private final String purl;

   /**
    * @param artifact
    *           (non-null) The Antenna artifact to get information from.
    */
   public ArtifactAdapter(Artifact artifact) {
      filename = figureOutArtifactFilename(artifact);
      copyrightStatement = artifact.askForGet(CopyrightStatement.class);
      licenses = createLicenses(ArtifactLicenseUtils.getFinalLicenses(artifact));
      purl = toPurl(artifact);
   }

   /**
    * Makes sure, a useable license text is there.
    *
    * @param license
    *           (non-null)
    * @return (non-null) A license text or according placeholder.
    */
   public static String getLicenseText(License license) {
      String text = license.getText();
      text = text != null ? text : "No license text available";
      // remove the magic unicode as it will likely not be in a unicode font set.
      return StringUtils.replace(text, "\uFEFF", "");
   }

   /**
    * Creates a key from license information that is unique and can be used as anchor.
    * 
    * @param license
    *           (non-null)
    * @return (non-null)
    */
   public static String createKey(License license) {
      // valid keys are likely: numbers and letters but no special chars or even '-'
      int hashCode = license.getName().hashCode();
      String prefix = hashCode < 0 ? "n" : "p";
      return prefix + Math.abs(hashCode);
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
      return Optional.ofNullable(purl);
   }

   private static String figureOutArtifactFilename(Artifact artifact) {
      return artifact.askFor(ArtifactFilename.class)
              .map(ArtifactAdapter::figureOutArtifactFilename)
              .orElse(artifact.askFor(ArtifactPathnames.class)
                      .map(ArtifactAdapter::figureOutArtifactFilename)
                      .filter(s -> s.equals("null"))
                      .orElse(""));
   }

   private static String figureOutArtifactFilename(ArtifactFilename artifactFilename) {
      return artifactFilename
              .getFilenames()
              .stream()
              .collect(Collectors.joining(", "));
   }

   private static String figureOutArtifactFilename(ArtifactPathnames artifactPathnames) {
      return artifactPathnames
              .get()
              .stream()
              .map(FilenameUtils::getName)
              .distinct()
              .collect(Collectors.joining(", "));
   }

   private static List<LicenseInfo> createLicenses(LicenseInformation licenseInformation) {
      return licenseInformation.getLicenses()
              .stream()
              .map(l -> new LicenseInfo(createKey(l), getLicenseText(l), l.getName(), l.getLongName()))
              .collect(Collectors.toList());
   }

   private static String toPurl(Artifact artifact) {
      return artifact.askFor(ArtifactCoordinates.class)
              .map(ArtifactCoordinates::getMainCoordinate)
              .map(l -> l.canonicalize())
              .orElse("");
   }
}
