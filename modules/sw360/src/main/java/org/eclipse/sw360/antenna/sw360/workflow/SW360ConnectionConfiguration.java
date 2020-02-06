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
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.http.HttpHeaders;

public class SW360ConnectionConfiguration {
    public static final String REST_SERVER_URL_KEY = "rest.server.url";
    public static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    public static final String USERNAME_KEY = "user.id";
    public static final String PASSWORD_KEY = "user.password";
    public static final String CLIENT_USER_KEY = "client.id";
    public static final String CLIENT_PASSWORD_KEY = "client.password";
    public static final String PROXY_USE = "proxy.use";

    private final String restServerUrl;
    private final String authServerUrl;
    private final String user;
    private final String password;
    private final String clientId;
    private final String clientPassword;

    private final SW360AuthenticationClient authenticationClient;

    public SW360ConnectionConfiguration(Getter<String> getConfigValue, Getter<Boolean> getBooleanConfigValue, String proxyHost, int proxyPort) {
        // SW360 Connection configuration
        restServerUrl = getConfigValue.apply(SW360ConnectionConfiguration.REST_SERVER_URL_KEY);
        authServerUrl = getConfigValue.apply(SW360ConnectionConfiguration.AUTH_SERVER_URL_KEY);
        user = getConfigValue.apply(SW360ConnectionConfiguration.USERNAME_KEY);
        password = getConfigValue.apply(SW360ConnectionConfiguration.PASSWORD_KEY);
        clientId = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_USER_KEY);
        clientPassword = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_PASSWORD_KEY);

        // Proxy configuration
        //TODO use proxy setting for template creation
        boolean proxyUse = getBooleanConfigValue.apply(SW360ConnectionConfiguration.PROXY_USE);
        ProxySettings proxySettings = new ProxySettings(proxyUse, proxyHost, proxyPort);
        System.out.println(proxySettings);

        this.authenticationClient = getSW360AuthenticationClient();
    }

    public SW360ConnectionConfiguration(String restServerUrl, String authServerUrl, String user, String password, String clientId, String clientPassword) {
        this.restServerUrl = restServerUrl;
        this.authServerUrl = authServerUrl;
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.clientPassword = clientPassword;

        this.authenticationClient = getSW360AuthenticationClient();
    }

    public SW360AuthenticationClient getSW360AuthenticationClient() {
        //TODO pass in shared REST template
        return new SW360AuthenticationClient(authServerUrl, null);
    }

    public SW360ComponentClientAdapter getSW360ComponentClientAdapter() {
        //TODO pass in shared REST template
        return new SW360ComponentClientAdapter(restServerUrl, null);
    }

    public SW360ReleaseClientAdapter getSW360ReleaseClientAdapter() {
        //TODO pass in shared REST template
        return new SW360ReleaseClientAdapter(restServerUrl, null);
    }

    public SW360LicenseClientAdapter getSW360LicenseClientAdapter() {
        //TODO pass in shared REST template
        return new SW360LicenseClientAdapter(restServerUrl, null);
    }

    public SW360ProjectClientAdapter getSW360ProjectClientAdapter() {
        //TODO pass in shared REST template
        return new SW360ProjectClientAdapter(restServerUrl, null);
    }

    public HttpHeaders getHttpHeaders() {
        return authenticationClient.getHeadersWithBearerToken(authenticationClient.getOAuth2AccessToken(user, password, clientId, clientPassword));
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s);
    }
}
