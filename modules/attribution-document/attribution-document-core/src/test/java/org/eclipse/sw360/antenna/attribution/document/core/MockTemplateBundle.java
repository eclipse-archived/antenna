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

import org.eclipse.sw360.antenna.attribution.document.core.TemplateBundle;
import org.eclipse.sw360.antenna.attribution.document.utils.TemplateLoaderUtil;

import java.io.InputStream;

/**
 * Used to test the service locater in {@link TemplateLoaderUtil}.
 */
public class MockTemplateBundle implements TemplateBundle {

   @Override
   public String key() {
      return "mock-bundle";
   }

   @Override
   public InputStream loadTitleTemplate() {
      return testTemplate();
   }

   @Override
   public InputStream loadCopyrightTemplate() {
      return testTemplate();
   }

   @Override
   public InputStream loadContentTemplate() {
      return testTemplate();
   }

   @Override
   public InputStream loadBackPageTemplate() {
      return testTemplate();
   }

   private InputStream testTemplate() {
      return MockTemplateBundle.class.getResourceAsStream( "/templates/test.pdf" );
   }

}
