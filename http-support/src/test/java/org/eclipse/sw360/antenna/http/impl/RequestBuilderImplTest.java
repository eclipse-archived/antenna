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
import org.eclipse.sw360.antenna.http.api.HttpExecutionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@code RequestBuilderImpl}. This class tests some
 * special corner cases which are not covered by the integration test class.
 */
public class RequestBuilderImplTest {
    @Test
    public void testHandlingOfJsonProcessingException() throws JsonProcessingException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        Object data = new Object();
        JsonProcessingException exception = mock(JsonProcessingException.class);
        when(mapper.writeValueAsString(data)).thenThrow(exception);
        RequestBuilderImpl builder = new RequestBuilderImpl(mapper);

        try {
            builder.bodyJson(data);
            fail("No exception thrown!");
        } catch (HttpExecutionException hex) {
            assertThat(hex.getCause()).isEqualTo(exception);
        }
    }
}