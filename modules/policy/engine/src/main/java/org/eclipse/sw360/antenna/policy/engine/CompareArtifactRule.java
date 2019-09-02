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

import java.util.Optional;

/**
 * {@link Rule} type that iterates through all {@link ThirdPartyArtifact} objects and allows to compare properties
 * of the artifact to the property of all other artifacts
 */
public interface CompareArtifactRule extends Rule {
    /**
     * @param leftArtifact Left side {@link ThirdPartyArtifact} of the comparison
     * @param rightArtifact Right side {@link ThirdPartyArtifact} of the comparison
     * @return {@link PolicyViolation} as {@link Optional}, if a policy violation is found an empty Optional if not
     */
    Optional<PolicyViolation> evaluate(ThirdPartyArtifact leftArtifact, ThirdPartyArtifact rightArtifact);
}
