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
package org.eclipse.sw360.antenna.sw360.workflow;

import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.rest.SW360RestTemplateFactory;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * A class for creating a new {@link SW360ConnectionConfiguration}.
 * </p>
 * <p>
 * This class processes configuration settings to create all supported
 * adapter objects to interact with a SW360 instance. The adapter objects can
 * be shared between multiple components; so it is safe to create them once
 * initially. They can then be obtained from the
 * {@link SW360ConnectionConfiguration} object returned by this factory.
 * </p>
 */
public class SW360ConnectionConfigurationFactory {
    /**
     * The factory for creating the shared Rest template.
     */
    private final SW360RestTemplateFactory restTemplateFactory;

    /**
     * Creates a new instance of {@code SW360ConnectionConfigurationFactory}
     * with default settings.
     */
    public SW360ConnectionConfigurationFactory() {
        this(new SW360RestTemplateFactory());
    }

    /**
     * Creates a new instance of {@code SW360ConnectionConfigurationFactory}
     * that uses the passed in {@code SW360RestTemplateFactory} to create the
     * {@code RestTemplate} shared by all SW360 adapter objects.
     *
     * @param restTemplateFactory the factory for creating a rest template
     */
    public SW360ConnectionConfigurationFactory(SW360RestTemplateFactory restTemplateFactory) {
        this.restTemplateFactory = restTemplateFactory;
    }

    /**
     * Creates a new instance of {@code SW360ConnectionConfiguration} that is
     * initialized from configuration data. The passed in {@code Getter}
     * objects are used to read in configuration settings. With the
     * {@code SW360RestTemplateFactory} provided, the central
     * {@code RestTemplate} is created that is used by all SW360 client and
     * adapter objects.
     *
     * @param getConfigValue        getter for string config settings
     * @param getBooleanConfigValue getter for boolean config settings
     * @param proxyHost             the proxy host
     * @param proxyPort             the proxy port
     * @return the new {@code SW360ConnectionConfiguration}
     */
    public SW360ConnectionConfiguration createConfiguration(Getter<String> getConfigValue,
                                                            Getter<Boolean> getBooleanConfigValue,
                                                            String proxyHost, int proxyPort) {
        ProxySettings settings = createProxySettings(getBooleanConfigValue, proxyHost, proxyPort);
        RestTemplate restTemplate = getRestTemplateFactory().createRestTemplate(settings);
        String restUrl = getConfigValue.apply(SW360ConnectionConfiguration.REST_SERVER_URL_KEY);
        String authUrl = getConfigValue.apply(SW360ConnectionConfiguration.AUTH_SERVER_URL_KEY);
        String user = getConfigValue.apply(SW360ConnectionConfiguration.USERNAME_KEY);
        String password = getConfigValue.apply(SW360ConnectionConfiguration.PASSWORD_KEY);
        String clientId = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_USER_KEY);
        String clientPassword = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_PASSWORD_KEY);

        SW360AuthenticationClient authClient = createAuthenticationClient(restTemplate, authUrl);
        SW360ComponentClientAdapter componentAdapter = createComponentAdapter(restTemplate, restUrl);
        SW360ReleaseClientAdapter releaseAdapter = createReleaseAdapter(restTemplate, restUrl, componentAdapter);
        SW360LicenseClientAdapter licenseAdapter = createLicenseAdapter(restTemplate, restUrl);
        SW360ProjectClientAdapter projectAdapter = createProjectAdapter(restTemplate, restUrl);

        return new SW360ConnectionConfiguration(authClient, componentAdapter, releaseAdapter, licenseAdapter,
                projectAdapter, user, password, clientId, clientPassword);
    }

    /**
     * Returns the {@code SW360RestTemplateFactory} that is used by this
     * object.
     *
     * @return the {@code SW360RestTemplateFactory}
     */
    public SW360RestTemplateFactory getRestTemplateFactory() {
        return restTemplateFactory;
    }

    /**
     * Creates the {@code SW360AuthenticationClient} for the configuration.
     *
     * @param restTemplate the rest template
     * @param authUrl      the URL to the authentication endpoint
     * @return the {@code SW360AuthenticationClient}
     */
    SW360AuthenticationClient createAuthenticationClient(RestTemplate restTemplate, String authUrl) {
        return new SW360AuthenticationClient(authUrl, restTemplate);
    }

    /**
     * Creates the {@code SW360ComponentClientAdapter} for the configuration.
     *
     * @param restTemplate the rest template
     * @param restUrl      the URL to the REST API endpoint
     * @return the {@code SW360ComponentClientAdapter}
     */
    SW360ComponentClientAdapter createComponentAdapter(RestTemplate restTemplate, String restUrl) {
        //TODO create correct component client
        return new SW360ComponentClientAdapter(null);
    }

    /**
     * Creates the {@code SW360ReleaseClientAdapter} for the configuration.
     *
     * @param restTemplate           the rest template
     * @param restUrl                the URL to the REST API endpoint
     * @param componentClientAdapter the {@code SW360ComponentClientAdapter}
     * @return the {@code SW360ReleaseClientAdapter}
     */
    SW360ReleaseClientAdapter createReleaseAdapter(RestTemplate restTemplate, String restUrl,
                                                   SW360ComponentClientAdapter componentClientAdapter) {
        //TODO create correct release client
        return new SW360ReleaseClientAdapter(null, componentClientAdapter);
    }

    /**
     * Creates the {@code SW360LicenseClientAdapter} for the configuration.
     *
     * @param restTemplate the rest template
     * @param restUrl      the URL to the REST API endpoint
     * @return the {@code SW360LicenseClientAdapter}
     */
    SW360LicenseClientAdapter createLicenseAdapter(RestTemplate restTemplate, String restUrl) {
        return new SW360LicenseClientAdapter()
                .setLicenseClient(new SW360LicenseClient(restUrl, restTemplate));
    }

    /**
     * Creates the {@code SW360ProjectClientAdapter} for the configuration.
     *
     * @param restTemplate the rest template
     * @param restUrl      the URL to the REST API endpoint
     * @return the {@code SW360ProjectClientAdapter}
     */
    SW360ProjectClientAdapter createProjectAdapter(RestTemplate restTemplate, String restUrl) {
        return new SW360ProjectClientAdapter()
                .setProjectClient(new SW360ProjectClient(restUrl, restTemplate));
    }

    /**
     * Creates the proxy settings to be used from the given configuration data.
     *
     * @param getBooleanConfigValue getter for boolean config settings
     * @param proxyHost             the proxy host
     * @param proxyPort             the proxy port
     * @return the {@code ProxySettings} to be used
     */
    private static ProxySettings createProxySettings(Getter<Boolean> getBooleanConfigValue,
                                                     String proxyHost, int proxyPort) {
        Boolean useProxy = getBooleanConfigValue.apply(SW360ConnectionConfiguration.PROXY_USE);
        return ProxySettings.fromConfig(useProxy, proxyHost, proxyPort);
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s);
    }
}
