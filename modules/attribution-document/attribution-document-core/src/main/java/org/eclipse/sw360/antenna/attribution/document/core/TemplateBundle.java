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
package org.eclipse.sw360.antenna.attribution.document.core;

import org.eclipse.sw360.antenna.attribution.document.utils.TemplateLoaderUtil;

import java.io.InputStream;
import java.util.Optional;

/**
 * Describes the templates to be used in document generation.
 *
 * This is used as service provider interface, so this is used to load implementations from classpath (via
 * {@link java.util.ServiceLoader}.
 * <p />
 * As there potentially might be multiple template bundles available, this must return a key, that is used by
 * {@link TemplateLoaderUtil} to select the correct implementation.
 * <p />
 * As an optional feature, you can provide your own fonts here. Typically this would mean to load a ttf-file from
 * classpath using PDType0Font.
 *
 * @see org.apache.pdfbox.pdmodel.font.PDType0Font#load(org.apache.pdfbox.pdmodel.PDDocument, InputStream)
 */
public interface TemplateBundle {
   /**
    * The key identifies this bundle.
    * <p />
    * It is intended to be used in the configuration in order to load the correct template. Thus, this should have a
    * meaningful name and to some degree unique.
    *
    * @return (non-null) the key for this bundle
    */
   String key();

   InputStream loadTitleTemplate();

   InputStream loadCopyrightTemplate();

   InputStream loadContentTemplate();

   InputStream loadBackPageTemplate();

   default Optional<InputStream> loadSansFont() {
      return Optional.empty();
   }

   default Optional<InputStream> loadSansItalicFont() {
      return Optional.empty();
   }

   default Optional<InputStream> loadSansBoldFont() {
      return Optional.empty();
   }

   default Optional<InputStream> loadSansBoldItalicFont() {
      return Optional.empty();
   }
}
