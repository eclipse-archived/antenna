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
package org.eclipse.sw360.antenna.policy.engine.testdata;

import org.eclipse.sw360.antenna.policy.engine.ThirdPartyArtifact;

import java.util.Arrays;
import java.util.Collection;

public class PolicyEngineTestdata {
    public static final String ALWAYS_VIOLATED_ID = "AV";
    public static final String NEVER_VIOLATED_ID = "NV";

    public static final String RULESET_CLASS = TestRuleset.class.getName();
    public static final String FAILING_RULESET_CLASS = FailingRuleset.class.getName();

    public static final Collection<String> RULESET_CONFIG = Arrays.asList(RULESET_CLASS);
    public static final Collection<String> FAILING_RULESET_CONFIG = Arrays.asList(FAILING_RULESET_CLASS);
    public static final Collection<String> NO_RULESET_CLASS_CONFIG = Arrays.asList(TestArtifact.class.getName());

    public static final Collection<ThirdPartyArtifact> ARTIFACTS = Arrays.asList(new TestArtifact(),
            new TestArtifact());
}
