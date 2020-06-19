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
package org.eclipse.sw360.antenna.attribution.document.core;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.sw360.antenna.attribution.document.utils.TemplateLoaderUtil;

class TemplateLoaderUtilTest {
   @Test
   void loadBundleFromFiles() {
      ClassLoader loader = getClass().getClassLoader();
      File cover = new File(loader.getResource("templates/antenna-demo_template_title.pdf").getFile());
      File copyright = new File(loader.getResource("templates/antenna-demo_template_copyright.pdf").getFile());
      File content = new File(loader.getResource("templates/antenna-demo_template_content.pdf").getFile());
      File back = new File(loader.getResource("templates/antenna-demo_template_back.pdf").getFile());

      Templates templates = TemplateLoaderUtil.load(cover, copyright, content, back);
      assertThat(templates.getTitle().getNumberOfPages()).isEqualTo(1);
      assertThat(templates.getCopyright().getNumberOfPages()).isEqualTo(1);
      assertThat(templates.getContent().getNumberOfPages()).isEqualTo(1);
      assertThat(templates.getBackPage().getNumberOfPages()).isEqualTo(1);
   }

   @Test
   void loadMockBundle() throws Exception {

      Templates templates = TemplateLoaderUtil.load( "mock-bundle" );
      assertThat( templates ).isNotNull();
      assertThat( templates.getTitle().getNumberOfPages() ).isEqualByComparingTo( 1 );
   }

   @Test
   void loadMissingBundleFails() throws Exception {

      assertThatExceptionOfType( IllegalStateException.class )
            .isThrownBy( () -> TemplateLoaderUtil.load( "-not-existing-key-" ) )
            .withMessage( "Unable to locate a template bundle for key '-not-existing-key-'. Please check your classpath or configuration." );

   }

}
