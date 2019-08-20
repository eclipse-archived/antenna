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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;

public abstract class SW360Client {
    private final boolean proxyUse;
    protected RestTemplate restTemplate;

    public abstract String getEndpoint();

    public SW360Client(boolean proxyUse, String proxyHost, int proxyPort) {
        this.proxyUse = proxyUse && proxyHost != null;
        if (proxyUse && proxyHost != null) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
            this.restTemplate = new RestTemplate(requestFactory);
        } else {
            this.restTemplate = new RestTemplate();
        }
    }

    protected <T> ResponseEntity<T> doRestCall(String url, HttpMethod method, HttpEntity<?> httpEntity, Class<T> responseType) {
        return this.restTemplate.
                exchange(url,
                        method,
                        httpEntity,
                        responseType);
    }

    protected <T> ResponseEntity<T> doRestCall(String url, HttpMethod method, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        return this.restTemplate.
                exchange(url,
                        method,
                        httpEntity,
                        responseType);
    }

    protected <T> ResponseEntity<T> doRestGET(String url, HttpHeaders header, ParameterizedTypeReference<T> responseType) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);
        return doRestCall(url, HttpMethod.GET, httpEntity, responseType);
    }

    protected <T> ResponseEntity<T> doRestPOST(String url, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        return doRestCall(url, HttpMethod.POST, httpEntity, responseType);
    }

    protected <T> ResponseEntity<T> doRestPATCH(String url, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> responseType) {
        if(proxyUse) {
            throw new UnsupportedOperationException("The patch functionality used when updating releases does not support proxy use");
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, responseType);
    }
}
