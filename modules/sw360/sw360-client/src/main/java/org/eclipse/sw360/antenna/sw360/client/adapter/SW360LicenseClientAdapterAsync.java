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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Service interface for an adapter supporting asynchronous operations on SW360
 * license entities.
 * </p>
 */
public interface SW360LicenseClientAdapterAsync {
    SW360LicenseClient getLicenseClient();

    CompletableFuture<Boolean> isLicenseOfArtifactAvailable(String license);

    CompletableFuture<Optional<SW360License>> getSW360LicenseByAntennaLicense(String license);

    CompletableFuture<Optional<SW360License>> getLicenseDetails(SW360SparseLicense sparseLicense);
}
