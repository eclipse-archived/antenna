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

import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

public abstract class SW360Client {
    private final boolean proxyUse;
    private final RestTemplate restTemplate;

    /**
     * Creates a new instance of {@code SW360Client} and initializes it with
     * the {@code RestTemplate} to be used for all HTTP requests.
     *
     * @param restTemplate the {@code RestTemplate}
     */
    protected SW360Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        proxyUse = false;
    }

    public abstract String getEndpoint();


    public SW360Client(ProxySettings proxySettings) {
        proxyUse = proxySettings.isProxyUse();
        if (proxyUse) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxySettings.getProxyHost(), proxySettings.getProxyPort()));
            requestFactory.setProxy(proxy);
            this.restTemplate = new RestTemplate(requestFactory);
        } else {
            this.restTemplate = new RestTemplate();
        }
    }

    protected <T> ResponseEntity<T> doRestCall(String url, HttpMethod method, HttpEntity<?> httpEntity, Class<T> responseType) {
        return this.getRestTemplate().
                exchange(url,
                        method,
                        httpEntity,
                        responseType);
    }

    protected <T> ResponseEntity<T> doRestCall(String url, HttpMethod method, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        return this.getRestTemplate().
                exchange(url,
                        method,
                        httpEntity,
                        responseType);
    }

    protected <T> ResponseEntity<T> doRestGET(String url, HttpHeaders header, ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(null, header);
        return doRestCall(url, HttpMethod.GET, httpEntity, responseType);
    }

    protected <T> ResponseEntity<T> doRestPOST(String url, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        return doRestCall(url, HttpMethod.POST, httpEntity, responseType);
    }

    protected <T> ResponseEntity<T> doRestPATCH(String url, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        return doRestCall(url, HttpMethod.PATCH, httpEntity, responseType);
    }

    /**
     * Returns the {@code RestTemplate} used by this client to interact with
     * the server. The template has been initialized from the connection
     * configuration and thus can be used for all requests.
     *
     * @return the {@code RestTemplate}
     */
    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
