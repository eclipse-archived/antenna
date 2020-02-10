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
package org.eclipse.sw360.antenna.sw360.workflow;

import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.springframework.http.HttpHeaders;

public class SW360ConnectionConfiguration {
    public static final String REST_SERVER_URL_KEY = "rest.server.url";
    public static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    public static final String USERNAME_KEY = "user.id";
    public static final String PASSWORD_KEY = "user.password";
    public static final String CLIENT_USER_KEY = "client.id";
    public static final String CLIENT_PASSWORD_KEY = "client.password";
    public static final String PROXY_USE = "proxy.use";

    private final SW360AuthenticationClient authenticationClient;
    private final SW360ComponentClientAdapter componentClientAdapter;
    private final SW360ReleaseClientAdapter releaseClientAdapter;
    private final SW360LicenseClientAdapter licenseClientAdapter;
    private final SW360ProjectClientAdapter projectClientAdapter;
    private final String user;
    private final String password;
    private final String clientId;
    private final String clientPassword;

    public SW360ConnectionConfiguration(Getter<String> getConfigValue, Getter<Boolean> getBooleanConfigValue, String proxyHost, int proxyPort) {
        // SW360 Connection configuration
        this.authenticationClient = null;
        this.componentClientAdapter = null;
        this.releaseClientAdapter = null;
        this.licenseClientAdapter = null;
        this.projectClientAdapter = null;
        user = null;
        password = null;
        clientId = null;
        clientPassword = null;
    }

    public SW360ConnectionConfiguration(SW360AuthenticationClient authenticationClient,
                                        SW360ComponentClientAdapter componentClientAdapter,
                                        SW360ReleaseClientAdapter releaseClientAdapter,
                                        SW360LicenseClientAdapter licenseClientAdapter,
                                        SW360ProjectClientAdapter projectClientAdapter,
                                        String user, String password,
                                        String clientId, String clientPassword) {
        this.authenticationClient = authenticationClient;
        this.componentClientAdapter = componentClientAdapter;
        this.releaseClientAdapter = releaseClientAdapter;
        this.licenseClientAdapter = licenseClientAdapter;
        this.projectClientAdapter = projectClientAdapter;
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.clientPassword = clientPassword;
    }

    public SW360AuthenticationClient getSW360AuthenticationClient() {
        return authenticationClient;
    }

    public SW360ComponentClientAdapter getSW360ComponentClientAdapter() {
        return componentClientAdapter;
    }

    public SW360ReleaseClientAdapter getSW360ReleaseClientAdapter() {
        return releaseClientAdapter;
    }

    public SW360LicenseClientAdapter getSW360LicenseClientAdapter() {
        return licenseClientAdapter;
    }

    public SW360ProjectClientAdapter getSW360ProjectClientAdapter() {
        return projectClientAdapter;
    }

    public HttpHeaders getHttpHeaders() {
        return authenticationClient.getHeadersWithBearerToken(authenticationClient.getOAuth2AccessToken(user, password, clientId, clientPassword));
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s);
    }
}
