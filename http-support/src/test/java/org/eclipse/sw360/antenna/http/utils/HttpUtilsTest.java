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
package org.eclipse.sw360.antenna.http.utils;

import org.eclipse.sw360.antenna.http.Response;
import org.eclipse.sw360.antenna.http.ResponseProcessor;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class HttpUtilsTest {
    @Test
    public void testWaitForSuccessfulFuture() throws IOException {
        Object result = new Object();
        CompletableFuture<Object> future = CompletableFuture.completedFuture(result);

        assertThat(HttpUtils.waitFor(future)).isEqualTo(result);
    }

    /**
     * Returns a future that fails with the given exception.
     *
     * @param exception the exception to fail the future with
     * @return the failing future
     */
    private static CompletableFuture<Object> failedFuture(Exception exception) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }

    @Test
    public void testWaitForFailedFutureWrappedException() {
        Exception exception = new IllegalStateException("Something went wrong");
        CompletableFuture<Object> future = failedFuture(exception);

        try {
            HttpUtils.waitFor(future);
            fail("No exception was thrown!");
        } catch (IOException e) {
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void testWaitForFailedFutureWithIOException() {
        Exception exception = new IOException("No connection");
        CompletableFuture<Object> future = failedFuture(exception);

        try {
            HttpUtils.waitFor(future);
            fail("No exception was thrown!");
        } catch (IOException e) {
            assertThat(e).isEqualTo(exception);
        }
    }

    @Test
    public void testWaitForInterrupted() {
        CompletableFuture<Object> neverCompletingFuture = new CompletableFuture<>();
        final Thread currentThread = Thread.currentThread();
        //start a new thread to interrupt this thread, which is waiting for the future
        Runnable interrupter = currentThread::interrupt;
        new Thread(interrupter).start();

        try {
            HttpUtils.waitFor(neverCompletingFuture);
            fail("No exception was thrown!");
        } catch (IOException e) {
            assertThat(e.getCause()).isInstanceOf(InterruptedException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }
    }

    /**
     * Creates a mock for a response processor.
     *
     * @return the new processor mock
     */
    private static ResponseProcessor<Object> createProcessorMock() {
        @SuppressWarnings("unchecked")
        ResponseProcessor<Object> mock = mock(ResponseProcessor.class);
        return mock;
    }

    @Test
    public void testCheckResponseSuccess() throws IOException {
        Response response = mock(Response.class);
        ResponseProcessor<Object> processor = createProcessorMock();
        Object result = new Object();
        when(response.isSuccess()).thenReturn(Boolean.TRUE);
        when(processor.process(response)).thenReturn(result);

        ResponseProcessor<Object> checkProcessor = HttpUtils.checkResponse(processor);
        Object processorResult = checkProcessor.process(response);
        assertThat(processorResult).isEqualTo(result);
    }

    @Test
    public void testCheckResponseFailed() throws IOException {
        final int status = 500;
        Response response = mock(Response.class);
        ResponseProcessor<Object> processor = createProcessorMock();
        when(response.isSuccess()).thenReturn(Boolean.FALSE);
        when(response.statusCode()).thenReturn(status);

        ResponseProcessor<Object> checkProcessor = HttpUtils.checkResponse(processor);
        try {
            checkProcessor.process(response);
            fail("No exception thrown!");
        } catch (FailedRequestException e) {
            assertThat(e.getMessage()).contains(String.valueOf(status));
            assertThat(e.getStatusCode()).isEqualTo(status);
            assertThat(e.getTag()).isNull();
            verifyZeroInteractions(processor);
        }
    }

    @Test
    public void testCheckResponseFailedWithTag() throws IOException {
        final int status = 400;
        final String tag = "MyTestRequest";
        Response response = mock(Response.class);
        ResponseProcessor<Object> processor = createProcessorMock();
        when(response.isSuccess()).thenReturn(Boolean.FALSE);
        when(response.statusCode()).thenReturn(status);

        ResponseProcessor<Object> checkProcessor = HttpUtils.checkResponse(processor, tag);
        try {
            checkProcessor.process(response);
            fail("No exception thrown!");
        } catch (FailedRequestException e) {
            assertThat(e.getMessage()).contains(String.valueOf(status), tag);
            assertThat(e.getStatusCode()).isEqualTo(status);
            assertThat(e.getTag()).isEqualTo(tag);
            verifyZeroInteractions(processor);
        }
    }

    @Test
    public void testUnwrapCompletionExceptionNull() {
        assertThat(HttpUtils.unwrapCompletionException(null)).isNull();
    }

    @Test
    public void testUnwrapCompletionExceptionNotWrapped() {
        Throwable exception = new IllegalStateException("test");

        assertThat(HttpUtils.unwrapCompletionException(exception)).isEqualTo(exception);
    }

    @Test
    public void testUnwrapCompletionExceptionWrapped() {
        Throwable exception = new IllegalStateException("testWrapped");
        Throwable wrap = new CompletionException(new CompletionException(exception));

        assertThat(HttpUtils.unwrapCompletionException(wrap)).isEqualTo(exception);
    }

    @Test
    public void testUnwrapCompletionExceptionEmptyWrap() {
        assertThat(HttpUtils.unwrapCompletionException(new CompletionException(null))).isNull();
    }

    @Test
    public void testUrlEncodeNull() {
        assertThat(HttpUtils.urlEncode(null)).isNull();
    }

    @Test
    public void testUrlEncode() {
        String source = "This is a (tricky ;-) test";
        String expResult = "This+is+a+%28tricky+%3B-%29+test";

        assertThat(HttpUtils.urlEncode(source)).isEqualTo(expResult);
    }

    @Test
    public void testAddQueryParametersNoParams() {
        String url = "https://test.antenna.org/test";

        assertThat(HttpUtils.addQueryParameters(url, Collections.emptyMap())).isEqualTo(url);
    }

    @Test(expected = NullPointerException.class)
    public void testAddQueryParametersNullURL() {
        HttpUtils.addQueryParameters(null, Collections.singletonMap("foo", "bar"));
    }

    @Test
    public void testAddQueryParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("foo", "bar");
        params.put("test", Boolean.TRUE);
        params.put("complex param", "this must be encoded");
        String url = "https://test.antenna.org/query";
        String expResult = url + "?foo=bar&test=true&complex+param=this+must+be+encoded";

        assertThat(HttpUtils.addQueryParameters(url, params)).isEqualTo(expResult);
    }

    @Test
    public void testAddQueryParameter() {
        String url = "https://test.antenna.org/query";
        String expResult = url + "?param=the+param";

        assertThat(HttpUtils.addQueryParameter(url, "param", "the param")).isEqualTo(expResult);
    }

    @Test
    public void testNullProcessor() throws IOException {
        Response response = mock(Response.class);
        ResponseProcessor<Void> processor = HttpUtils.nullProcessor();

        assertThat(processor.process(response)).isNull();
        verifyZeroInteractions(response);
    }
}
