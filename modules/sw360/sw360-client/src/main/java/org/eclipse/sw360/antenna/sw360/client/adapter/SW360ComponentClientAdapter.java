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

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;

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
}
