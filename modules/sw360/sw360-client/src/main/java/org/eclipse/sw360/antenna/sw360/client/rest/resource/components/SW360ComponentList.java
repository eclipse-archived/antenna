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
package org.eclipse.sw360.antenna.sw360.client.rest.resource.components;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Paging;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;

public class SW360ComponentList extends SW360HalResource<LinkObjects, SW360ComponentListEmbedded> {
    private Paging page;

    public Paging getPage() {
        return page;
    }

    public void setPage(Paging page) {
        this.page = page;
    }

    @Override
    public LinkObjects createEmptyLinks() {
        return new LinkObjects();
    }

    @Override
    public SW360ComponentListEmbedded createEmptyEmbedded() {
        return new SW360ComponentListEmbedded();
    }
}