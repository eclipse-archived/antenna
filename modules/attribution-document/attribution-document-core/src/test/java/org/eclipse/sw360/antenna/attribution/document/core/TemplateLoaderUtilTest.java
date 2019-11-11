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

import static org.assertj.core.api.Assertions.*;

import org.eclipse.sw360.antenna.attribution.document.core.Templates;
import org.eclipse.sw360.antenna.attribution.document.utils.TemplateLoaderUtil;
import org.junit.jupiter.api.Test;

class TemplateLoaderUtilTest {
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
