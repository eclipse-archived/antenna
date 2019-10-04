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
package org.eclipse.sw360.antenna.disclosure.document.demo;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sw360.antenna.disclosure.document.core.TemplateBundle;

/**
 *
 */
public class DemoTemplateBundle implements TemplateBundle {

   @Override
   public String key() {
      return "antenna-demo";
   }

   @Override
   public InputStream loadTitleTemplate() {
      return load( "/templates/antenna-demo_template_title.pdf" );
   }

   @Override
   public InputStream loadCopyrightTemplate() {
      return load( "/templates/antenna-demo_template_copyright.pdf" );
   }

   @Override
   public InputStream loadContentTemplate() {
      return load( "/templates/antenna-demo_template_content.pdf" );
   }

   @Override
   public InputStream loadBackPageTemplate() {
      return load( "/templates/antenna-demo_template_back.pdf" );
   }

   private InputStream load( String resourceName ) {
      InputStream stream = DemoTemplateBundle.class.getResourceAsStream( resourceName );
      Objects.requireNonNull( stream, "Unable to load resource for '" + resourceName + "'. Maybe the name is wrong or the packaging?" );
      return stream;
   }

}
