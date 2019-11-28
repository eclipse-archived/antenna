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
package org.eclipse.sw360.antenna.attribution.document.core.model;

import java.util.List;
import java.util.Optional;

/**
 * The information on artifact (and license(s)) that can be processed in attribution document generation.
 */
public interface ArtifactAndLicense {

    /**
     * @return (non - null) the originating file name
     */
    String getFilename();

    /**
     * @return (non - null) the package-url as String representation. This should be present if the artifact is a component
     * (e.g. a Maven artifact, a NPM module, ..)
     * @see <a href="https://github.com/package-url/purl-spec">PURL specification</a>
     */
    Optional<String> getPurl();

    /**
     * @return (non - null) information about license(s).
     */
    List<LicenseInfo> getLicenses();

    /**
     * @return (non - null) the copyright statement if there is one necessary
     */
    Optional<String> getCopyrightStatement();
}
