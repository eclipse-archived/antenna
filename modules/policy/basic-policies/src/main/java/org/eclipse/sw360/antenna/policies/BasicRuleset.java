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
package org.eclipse.sw360.antenna.policies;

import org.eclipse.sw360.antenna.policy.engine.Rule;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicRuleset implements Ruleset {
    @Override
    public String getName() {
        return "Basic Policy Rules";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Collection<Rule> getRules() {
        return Stream.of(
                new ArtifactKnownRule(this),
                new ArtifactLicenseKnownRule(this),
                new LicenseQualifiedRule(this),
                new LicenseSelectedRule(this),
                new QualifiedComplexLicenseRule(this),
                new ArtifactLicenseQualifiedRule(this),
                new SourcesAvailableRule(this),
                new SourcesKnownRule(this),
                new SWHSourceIdKnownRule(this))
                .collect(Collectors.toList());
    }
}
