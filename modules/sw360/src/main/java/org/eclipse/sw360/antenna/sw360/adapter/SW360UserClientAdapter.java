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

package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.SW360UserClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.http.HttpHeaders;

public class SW360UserClientAdapter {

    private final SW360UserClient userClient;

    public SW360UserClientAdapter(String restUrl, ProxySettings proxySettings) {
        this.userClient= new SW360UserClient(restUrl, proxySettings);
    }

    public SW360User getUserByEmail(String userId, HttpHeaders header) throws ExecutionException {
        return userClient.getUserByEmail(userId, header);
    }
}
