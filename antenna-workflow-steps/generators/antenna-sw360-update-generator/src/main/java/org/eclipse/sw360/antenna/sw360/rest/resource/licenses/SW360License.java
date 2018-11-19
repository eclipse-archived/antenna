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

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

public class SW360License<L extends LinkObjects, E extends Embedded> extends SW360HalResource<L, E> {
    private String text;
    private String shortName;
    private String fullName;

    public SW360License() { }

    public SW360License(License license) {
        this.fullName = license.getLongName();
        this.shortName = license.getName();
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
}
