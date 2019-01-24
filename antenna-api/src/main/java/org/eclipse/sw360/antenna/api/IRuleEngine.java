/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.api;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

/**
 * Provide an API for calling Antenna Rule Engines.
 */
public interface IRuleEngine {
    /**
     * Calls on the rule engine to apply the policies to the analysed project.
     *
     * @return Results of evaluation.
     */
    IPolicyEvaluation evaluate(Collection<Artifact> artifacts) throws AntennaException;

    /**
     * @return The version of this rule set.
     */
    Optional<String> getRulesetVersion();
}
