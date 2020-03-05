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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@code RequestBuilderImpl}. This class tests some
 * special corner cases which are not covered by the integration test class.
 */
public class RequestBuilderImplTest {
    /**
     * Mock for the JSON mapper.
     */
    private ObjectMapper mapper;

    /**
     * The builder to be tested.
     */
    private RequestBuilderImpl requestBuilder;

    @Before
    public void setUp() {
        mapper = mock(ObjectMapper.class);
        requestBuilder = new RequestBuilderImpl(mapper);
    }

    @Test
    public void testHandlingOfJsonProcessingException() throws JsonProcessingException {
        Object data = new Object();
        JsonProcessingException exception = mock(JsonProcessingException.class);
        when(mapper.writeValueAsString(data)).thenThrow(exception);

        try {
            requestBuilder.bodyJson(data);
            fail("No exception thrown!");
        } catch (IllegalStateException iex) {
            assertThat(iex.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void testBodyFileNoFileName() {
        Path folderPath = Paths.get("/");

        requestBuilder.bodyFile(folderPath, "text/plain");
        assertThat(requestBuilder.getFileName()).isNull();
    }
}
