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

import java.util.Collection;

/**
 * This is a collection of {@link Rule} objects within a common context. The configuration of the {@link PolicyEngine}
 * in the {@link PolicyEngineConfigurator} allows to give several rule sets to set up the policies.
 */
public interface RuleSet {
    String name();
    String version();
    Collection<Rule> rules();
}
