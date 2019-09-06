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

import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

/**
 * {@link Rule} type that iterates through all {@link ThirdPartyArtifact} objects and allows to define
 * a local property check on the associated metadata.
 */
public interface SingleArtifactRule extends Rule {
    /**
     * @param thirdPartyArtifact The {@link ThirdPartyArtifact} for which a property check is executed
     * @return {@link PolicyViolation} as {@link Optional}, if a policy violation is found an empty Optional if not
     */
    Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact);
}
