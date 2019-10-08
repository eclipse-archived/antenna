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

/**
 * This enumeration allows to differentiate between the different kinds of license information, i.e., it distinguishes
 * between no license information, only declared license information (no analysis of artifact source to identify
 * observed licenses), only observed (no declaration of license found), a deducted license expression out of
 * declared and observed (sufficient for simple cases and consistent findings), or, a explicitly set license (in case
 * of an unclear situation and a manual overwrite by an open source expert).
 */
public enum LicenseState {
    NO_LICENSE,
    DECLARED_ONLY,
    OBSERVED_ONLY,
    DECLARED_AND_OBSERVED,
    EXPLICITLY_SET
}
