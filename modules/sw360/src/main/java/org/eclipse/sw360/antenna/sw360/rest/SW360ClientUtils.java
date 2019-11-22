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
package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentList;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SW360ClientUtils {

    private SW360ClientUtils() {
        // Utils
    }

    static List<SW360SparseRelease> getSw360SparseReleases(ResponseEntity<Resource<SW360ReleaseList>> response) {
        checkRestStatus(response);
        SW360ReleaseList resource = getSaveOrThrow(response.getBody(), Resource::getContent);

        if (resource.get_Embedded() != null &&
                resource.get_Embedded().getReleases() != null) {
            return resource.get_Embedded().getReleases();
        } else {
            return new ArrayList<>();
        }
    }

    static List<SW360SparseComponent> getSw360SparseComponents(ResponseEntity<Resource<SW360ComponentList>> response) {
        checkRestStatus(response);
        SW360ComponentList resource = getSaveOrThrow(response.getBody(), Resource::getContent);

        if (resource.get_Embedded() != null &&
                resource.get_Embedded().getComponents() != null) {
            return resource.get_Embedded().getComponents();
        } else {
            return new ArrayList<>();
        }
    }

    static void checkRestStatus(ResponseEntity response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ExecutionException(response.getStatusCode().toString());
        }
    }

    interface Getter <T, I> {
        I get(T item);
    }

    static <T, I> I getSaveOrThrow(T item, Getter<T, I> getter) {
        return Optional.ofNullable(item)
                .map(getter::get)
                .orElseThrow(IllegalStateException::new);
    }
}
