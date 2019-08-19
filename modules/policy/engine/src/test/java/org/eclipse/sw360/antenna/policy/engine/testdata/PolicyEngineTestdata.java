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
    public static final String RULESETCLASS = TestRuleSet.class.getName();
    public static final String FAILINGRULESETCLASS = FailingRuleSet.class.getName();

    public static final Collection<String> RULESETCONFIG = Arrays.asList(RULESETCLASS);
    public static final Collection<String> FAILINGRULESETCONFIG = Arrays.asList(FAILINGRULESETCLASS);
    public static final Collection<ThirdPartyArtifact> ARTIFACTS = Arrays.asList(new TestArtifact(), new TestArtifact());
}
