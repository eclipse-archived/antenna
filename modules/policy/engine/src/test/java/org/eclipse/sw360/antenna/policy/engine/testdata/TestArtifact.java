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

import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseData;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseState;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TestArtifact implements ThirdPartyArtifact {
    @Override
    public boolean isProprietary() {
        return false;
    }

    @Override
    public LicenseState getLicenseState() {
        return LicenseState.EXPLICITLY_SET;
    }

    @Override
    public Collection<LicenseData> getLicenses() {
        return Collections.emptyList();
    }

    @Override
    public Optional<String> getLicenseExpression() {
        return Optional.of("X AND Y");
    }

    @Override
    public Optional<URL> getSourceFileOrLink() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSWHSourceId() {
        return Optional.empty();
    }

    @Override
    public Collection<Coordinate> getCoordinates() {
        return Collections.singletonList(new Coordinate("pkg:generic/unknown"));
    }
}
