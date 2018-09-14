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

import java.util.Set;

import org.eclipse.sw360.antenna.model.Artifact;

public interface IEvaluationResult {
    enum Severity {
        INFO,
        WARN,
        FAIL;

        public String value() {
            return name();
        }

        public static Severity fromValue(String v) {
            return valueOf(v);
        }
    };

    /**
     * @return ID of the evaluated rule.
     */
    String getId();

    /**
     * @return Description of the evaluated rule.
     */
    String getDescription();

    /**
     * @return The severity of breaking this rule.
     */
    Severity getSeverity();

    /**
     * @return All artifacts that have broken this rule.
     */
    Set<Artifact> getFailedArtifacts();
}
