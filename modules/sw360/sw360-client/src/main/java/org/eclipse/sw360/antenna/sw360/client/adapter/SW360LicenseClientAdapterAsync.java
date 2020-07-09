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

import org.eclipse.sw360.antenna.sw360.client.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360SparseLicense;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Service interface for an adapter supporting asynchronous operations on SW360
 * license entities.
 * </p>
 */
public interface SW360LicenseClientAdapterAsync {
    /**
     * Returns the {@code SW360LicenseClient} used for the interaction with
     * the SW360 server.
     *
     * @return the underlying {@code SW360LicenseClient}
     */
    SW360LicenseClient getLicenseClient();

    /**
     * Returns a list with all licenses known to the system.
     *
     * @return a future with the list with all the licenses known
     */
    CompletableFuture<List<SW360SparseLicense>> getLicenses();

    CompletableFuture<Boolean> isLicenseOfArtifactAvailable(String license);

    CompletableFuture<Optional<SW360License>> getSW360LicenseByAntennaLicense(String license);

    CompletableFuture<Optional<SW360License>> getLicenseDetails(SW360SparseLicense sparseLicense);
}
