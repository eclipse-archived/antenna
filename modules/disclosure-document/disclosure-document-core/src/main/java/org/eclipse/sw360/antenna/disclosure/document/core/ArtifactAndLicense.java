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
package org.eclipse.sw360.antenna.disclosure.document.core;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

/**
 * The information on artifact (and license(s)) that can be processed in disclosure document generation.
 */
public interface ArtifactAndLicense {

   /**
    * @return (non-null) the originating file name
    */
   String getFilename();

   /**
    * @see https://github.com/package-url/purl-spec
    * @return (non-null) the package-url as String representation. This should be present if the artifact is a component
    *         (e.g. a Maven artifact, a NPM module, ..)
    */
   Optional<String> getPurl();

   /**
    * @return (non-null) information about license(s).
    */
   List<LicenseInfo> getLicenses();

   /**
    * @return (non-null) the copyright statement if there is one necessary
    */
   Optional<String> getCopyrightStatement();

   class LicenseInfo {

      private final String key, title, text, shortName;

      /**
       * @param key
       *           (non-null) A key that identifies the license and that can be used as anchor in PDFBox
       * @param text
       *           (non-null) The license text.
       * @param shortName
       *           (non-null) the shortname of the license.
       * @param title
       *           (nullable) The title. If not set, the shortName will be used instead.
       */
      public LicenseInfo( String key, String text, String shortName, String title ) {
         Validate.isTrue( key != null, "Key must not be null" );
         Validate.isTrue( shortName != null, "Shortname must not be null" );
         Validate.isTrue( text != null, "License text must nut be null" );
         this.key = key;
         this.shortName = shortName;
         this.title = title;
         this.text = text;
      }

      public String getKey() {
         return key;
      }

      public String getTitle() {
         if ( title == null ) {
            return getShortName();
         }
         return title;
      }

      public String getText() {
         return text;
      }

      public String getShortName() {
         return shortName;
      }
   }
}
