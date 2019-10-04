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
package org.eclipse.sw360.antenna.disclosure.document.workflow.generators;

import java.util.Comparator;

import org.eclipse.sw360.antenna.disclosure.document.core.ArtifactAndLicense;

/**
 * Comparator used to sort artifact and licenses.
 */
public class ArtifactAndLicenseComparator implements Comparator<ArtifactAndLicense> {
   @Override
   public int compare( ArtifactAndLicense o1, ArtifactAndLicense o2 ) {
      boolean b1 = o1.getPurl().isPresent();
      boolean b2 = o2.getPurl().isPresent();

      int compared = 0;
      if ( b1 && b2 ) {
         compared = String.CASE_INSENSITIVE_ORDER.compare( o1.getPurl().get(), o2.getPurl().get() );
      }
      if ( compared == 0 ) {
         compared = String.CASE_INSENSITIVE_ORDER.compare( o1.getFilename(), o2.getFilename() );
      }
      return compared;
   }
}