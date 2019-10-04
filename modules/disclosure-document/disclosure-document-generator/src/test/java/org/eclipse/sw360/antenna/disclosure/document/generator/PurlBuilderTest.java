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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates.JavaScriptCoordinatesBuilder;
import org.junit.jupiter.api.Test;

/**
 *
 */
class PurlBuilderTest {

   @Test
   void angularNpm() throws Exception {

      JavaScriptCoordinatesBuilder builder = new JavaScriptCoordinatesBuilder();
      builder.setArtifactId( "@angular/animation-12.3.1" );
      builder.setName( "@angular/animation" );
      builder.setVersion( "12.3.1" );

      String purl = PurlBuilder.toPurl( builder.build() );
      assertThat( purl ).isEqualTo( "pkg:npm/%40angular/animation@12.3.1" );

   }

   @Test
   void npm() throws Exception {

      JavaScriptCoordinatesBuilder builder = new JavaScriptCoordinatesBuilder();
      builder.setArtifactId( "foobar-12.3.1" );
      builder.setName( "foobar" );
      builder.setVersion( "12.3.1" );

      String purl = PurlBuilder.toPurl( builder.build() );
      assertThat( purl ).isEqualTo( "pkg:npm/foobar@12.3.1" );

   }

}
