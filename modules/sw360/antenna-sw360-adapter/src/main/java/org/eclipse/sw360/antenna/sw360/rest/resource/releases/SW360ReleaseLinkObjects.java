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
package org.eclipse.sw360.antenna.sw360.rest.resource.releases;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;

public class SW360ReleaseLinkObjects extends LinkObjects {
    @JsonProperty("sw360:component")
    private Self selfComponent;

    public Self getSelfComponent() {
        return this.selfComponent;
    }

    public SW360ReleaseLinkObjects setSelfComponent(Self selfComponent)	{
        this.selfComponent = selfComponent;
        return this;
    }
}
