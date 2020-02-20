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
package org.eclipse.sw360.antenna.http.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.api.RequestBuilder;
import org.eclipse.sw360.antenna.http.api.RequestProducer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit test class for {@code MultipartRequestBuilder}. This class only tests
 * the functionality which is not covered by integration tests.
 */
public class MultipartRequestBuilderTest {
    /**
     * The builder to be tested.
     */
    private MultipartRequestBuilder builder;

    @Before
    public void setUp() {
        builder = new MultipartRequestBuilder(new ObjectMapper(), "partName");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUriCannotBeSet() {
        builder.uri("https://projects.eclipse.org/projects/technology.sw360.antenna");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMethodCannotBeSet() {
        builder.method(RequestBuilder.Method.GET);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBodyPartCannotBeAdded() {
        builder.bodyPart("partInPart", mock(RequestProducer.class));
    }
}