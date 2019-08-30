/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.policy.engine;

import com.github.packageurl.PackageURL;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface is a facade to a third party artifact. It defines a lightweight dsl needed for policy
 * evaluations that can be implemented for different models through implementing this interface.
 */
public interface ThirdPartyArtifact {
    /**
     * @return True, if the artifact is identified as a known Open Source component, False, if the component is not known
     * and needs an identification process to clarify the component source.
     */
    boolean isIdentified();

    /**
     * @param searchedLicenses A list of licenses in the form of a SPDX license reference, e.g., GPL-2.0-only. The
     *                         method goes through the licenses of the artifact and returns the intersection between
     *                         the given list and the licenses found in the artifact.
     * @return The intersection between the license strings given as search list and the licenses found in the artifact.
     */
    Collection<String> hasLicenses(Collection<String> searchedLicenses);

    /**
     * @param searchedLicenses A list of licenses in the form of a SPDX license reference, e.g., GPL-2.0-only. The
     *                         method goes through the licenses of the artifact and returns whether the intersection
     *                         between the given list and the licenses found in the artifact is not empty.
     * @return True, if at least on of the given licenses is part of the artifacts license expression, False, if not.
     */
    default boolean hasAtLeastOneLicenseOf(Collection<String> searchedLicenses) {
        return !hasLicenses(searchedLicenses).isEmpty();
    }

    /**
     * @return The component coordinates represented as a purl or an empty Optional, if the artifact information is not
     * identified and contains not enough information to at least define a basic purl.
     */
    Optional<PackageURL> getPurl();
}
