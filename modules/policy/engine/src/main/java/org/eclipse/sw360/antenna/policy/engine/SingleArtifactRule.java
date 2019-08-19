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
 * {@link Rule} type that iterates through all found {@link ThirdPartyArtifact} objects and allows to define
 * a local property check on the associated metadata.
 */
public interface SingleArtifactRule extends Rule {
    /**
     * @param thirdPartyArtifact The {@link ThirdPartyArtifact} for which a property check is executed
     * @throws PolicyException If the policy condition is violated
     */
    void evaluate(ThirdPartyArtifact thirdPartyArtifact) throws PolicyException;
}
