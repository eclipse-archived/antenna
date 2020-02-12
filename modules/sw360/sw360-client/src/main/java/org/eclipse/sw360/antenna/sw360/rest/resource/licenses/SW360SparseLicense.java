/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest.resource.licenses;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.sw360.antenna.sw360.rest.resource.*;

import java.util.Objects;

public class SW360SparseLicense extends SW360HalResource<LinkObjects, Embedded> {
    private String fullName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getFullName() {
        return this.fullName;
    }

    public SW360SparseLicense setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getShortName() {
        return SW360HalResourceUtility.getLastIndexOfSelfLink(get_Links()).orElse("");
    }

    public SW360SparseLicense setShortName(String shortName) {
        get_Links().setSelf(new Self(shortName));
        return this;
    }

    @Override
    public LinkObjects createEmptyLinks() {
        return new LinkObjects();
    }

    @Override
    public Embedded createEmptyEmbedded() {
        return new EmptyEmbedded();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SW360SparseLicense that = (SW360SparseLicense) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fullName);
    }
}
