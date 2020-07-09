/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.MultiStatusResponse;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Service interface for an adapter supporting operations on SW360 component
 * entities.
 * </p>
 */
public interface SW360ComponentClientAdapter {
    SW360ComponentClient getComponentClient();

    Optional<SW360Component> getOrCreateComponent(SW360Component componentFromRelease);

    SW360Component createComponent(SW360Component component);

    Optional<SW360Component> getComponentById(String componentId);

    Optional<SW360Component> getComponentByName(String componentName);

    List<SW360SparseComponent> getComponents();

    /**
     * Triggers a multi-delete operation for the components with the IDs
     * specified. Returns a {@code MultiStatusResponse} that allows checking
     * whether all the components could be deleted successfully.
     *
     * @param idsToDelete a collection with the IDs of components to delete
     * @return a {@code MultiStatusResponse} with the results of the operation
     */
    MultiStatusResponse deleteComponents(Collection<String> idsToDelete);

    /**
     * Deletes the component with the given ID. This is a convenience method for
     * the special case that only a single component should be deleted. It
     * inspects the {@link MultiStatusResponse} returned by SW360 and throws an
     * exception if the operation was not successful.
     *
     * @param componentId the ID of the component to be deleted
     * @throws org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException if the component could not be deleted
     */
    void deleteComponent(String componentId);
}
