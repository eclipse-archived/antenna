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

import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

import java.util.Objects;

public class SW360License extends SW360HalResource<LinkObjects, Embedded> {
    private String text;
    private String shortName;
    private String fullName;

    public SW360License() { }

    public SW360License(License license) {
        this.fullName = license.getName();
        this.shortName = license.getLicenseId();
        this.text = license.getText();
    }

    public String getText() {
        return this.text;
    }

    public SW360License setText(String text) {
        this.text = text;
        return this;
    }

    public String getShortName() {
        return this.shortName;
    }

    public SW360License setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public String getFullName() {
        return this.fullName;
    }

    public SW360License setFullName(String fullName) {
        this.fullName = fullName;
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
        SW360License that = (SW360License) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(shortName, that.shortName) &&
                Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), text, shortName, fullName);
    }
}
