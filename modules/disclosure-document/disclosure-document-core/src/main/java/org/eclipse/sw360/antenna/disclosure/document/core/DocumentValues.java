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

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * The values that are set (via configuration) in the document.
 */
public class DocumentValues {

   private final String productName;
   private final String version;
   private final String copyrightHolder;
   private final String copyrightYear;

   public DocumentValues( String productName, String version, String copyrightHolder ) {
      Validate.isTrue( StringUtils.isNotBlank( productName ) );
      Validate.isTrue( StringUtils.isNotBlank( version ) );
      Validate.isTrue( StringUtils.isNotBlank( copyrightHolder ) );
      this.productName = productName;
      this.version = version;
      this.copyrightHolder = copyrightHolder;
      this.copyrightYear = currentYear();
   }

   public String getProductName() {
      return productName;
   }

   public static String currentYear() {
      return String.valueOf( LocalDate.now().getYear() );
   }

   public String getVersion() {
      return version;
   }

   public String getCopyrightHolder() {
      return copyrightHolder;
   }

   public String getCopyrightYear() {
      return copyrightYear;
   }
}
