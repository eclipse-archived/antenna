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

/**
 * This interface is a facade to a third party artifact. It defines a lightweight dsl needed for policy
 * evaluations that can be implemented for different models through implementing this interface.
 */
public interface ThirdPartyArtifact {
    /**
     * @return True, if the artifact is identified as a known Open Source component, False, if the component is not known
     *         and needs an identification process to clarify the component source
     */
    boolean isIdentified();

    /**
     * @param licenseRegex A regular expression representing a family of licenses in the form of a SPDX license reference,
     *                     e.g., "GPL.*" for all GPL strings like GPL-2.0-only
     * @return True, if a license that matches the pattern is found, False, if the license is not found
     */
    boolean hasLicense(String licenseRegex);

    /**
     * @return The component coordinates represented as a purl
     */
    String getPurl();
}
