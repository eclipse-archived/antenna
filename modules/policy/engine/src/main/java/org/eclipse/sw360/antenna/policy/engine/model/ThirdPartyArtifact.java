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
package org.eclipse.sw360.antenna.policy.engine.model;

import org.eclipse.sw360.antenna.model.coordinates.Coordinate;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface is a facade to a third party artifact. It defines a lightweight dsl needed for policy
 * evaluations that can be implemented for different models through implementing this interface.
 */
public interface ThirdPartyArtifact {
    /**
     * @return True, if the artifact is not an Open Source component but licenses proprietary instead, False, if
     * it is an Open Source component.
     */
    boolean isProprietary();

    /**
     * @return The state of the license expression, is it determined out of declared and observed, is it explicitely
     * set by an open source expert or is it only partially available (only declared or only observed).
     */
    LicenseState getLicenseState();

    /**
     * @return The list of licenses attached to the artifact
     */
    Collection<LicenseData> getLicenses();

    /**
     * @return The effective license expression valid for the artifact as a SPDX expression
     */
    Optional<String> getLicenseExpression();

    /**
     * @return Either a link to the local archive that contains the sources or a link to the source repo
     */
    Optional<URL> getSourceFileOrLink();

    /**
     * @return The Software Heritage Id of the artifact sources
     */
    Optional<String> getSWHSourceId();

    /**
     * @return The component coordinates represented as a purl or an empty Optional, if the artifact information is not
     * identified and contains not enough information to at least define a basic purl.
     */
    Collection<Coordinate> getCoordinates();
}
