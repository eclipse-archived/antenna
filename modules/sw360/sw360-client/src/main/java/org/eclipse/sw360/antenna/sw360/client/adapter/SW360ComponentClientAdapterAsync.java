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
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Service interface for an adapter supporting asynchronous operations on SW360
 * component entities.
 * </p>
 */
public interface SW360ComponentClientAdapterAsync {
    SW360ComponentClient getComponentClient();

    CompletableFuture<Optional<SW360Component>> getOrCreateComponent(SW360Component componentFromRelease);

    CompletableFuture<SW360Component> createComponent(SW360Component component);

    CompletableFuture<Optional<SW360Component>> getComponentById(String componentId);

    CompletableFuture<Optional<SW360Component>> getComponentByName(String componentName);

    CompletableFuture<List<SW360SparseComponent>> getComponents();
}
