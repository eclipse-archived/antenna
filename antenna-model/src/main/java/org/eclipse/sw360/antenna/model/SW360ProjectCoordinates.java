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

package org.eclipse.sw360.antenna.model;

import org.eclipse.sw360.antenna.model.xml.generated.FromXmlSW360ProjectCoordinates;

import java.util.Objects;

public class SW360ProjectCoordinates {
    private FromXmlSW360ProjectCoordinates project;

    public SW360ProjectCoordinates(FromXmlSW360ProjectCoordinates project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SW360ProjectCoordinates)) return false;
        SW360ProjectCoordinates that = (SW360ProjectCoordinates) o;
        return Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project);
    }

    public String getName() {
        return project.getName();
    }

    public String getVersion() {
        return project.getVersion();
    }
}