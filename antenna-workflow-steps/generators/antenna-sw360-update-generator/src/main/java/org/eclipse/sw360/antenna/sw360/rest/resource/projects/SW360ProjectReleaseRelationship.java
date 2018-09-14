/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest.resource.projects;

public class SW360ProjectReleaseRelationship {
    public SW360ReleaseRelationship releaseRelation;
    public SW360MainlineState mainlineState;

    public SW360ProjectReleaseRelationship() {
    }

    public SW360ProjectReleaseRelationship(SW360ReleaseRelationship releaseRelation, SW360MainlineState mainlineState) {
        this.releaseRelation = releaseRelation;
        this.mainlineState = mainlineState;
    }

    public SW360ReleaseRelationship getReleaseRelation() {
        return this.releaseRelation;
    }

    public SW360ProjectReleaseRelationship setReleaseRelation(SW360ReleaseRelationship releaseRelation) {
        this.releaseRelation = releaseRelation;
        return this;
    }

    public SW360MainlineState getMainlineState() {
        return this.mainlineState;
    }

    public SW360ProjectReleaseRelationship setMainlineState(SW360MainlineState mainlineState) {
        this.mainlineState = mainlineState;
        return this;
    }
}
