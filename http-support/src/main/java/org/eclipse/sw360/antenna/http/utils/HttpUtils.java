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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.api.RequestProducer;
import org.eclipse.sw360.antenna.http.api.Response;
import org.eclipse.sw360.antenna.http.api.ResponseProcessor;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

/**
 * <p>
 * A class providing static utility functions related to the usage of
 * {@link org.eclipse.sw360.antenna.http.api.HttpClient} objects.
 * </p>
 */
public final class HttpUtils {
    /**
     * A predicate that can be used by the
     * {@link #checkResponse(ResponseProcessor, Predicate)} method to check
     * whether a response was successful. This predicate returns
     * <strong>true</strong> if and only if the passed in {@code Response}
     * reports itself as successful.
     *
     * @see Response#isSuccess()
     */
    public static Predicate<Response> SUCCESS_STATUS = Response::isSuccess;

    /**
     * Private constructor to prevent the creation of instances.
     */
    private HttpUtils() {
    }

    /**
     * Blocks until the given future is completed, returns its result, and
     * handles occurring exceptions. {@code HttpClient} has an asynchronous
     * API, but this method offers an easy way to transform this to a blocking
     * programming model by just waiting for the result to become available.
     * To simplify exception handling for the caller, the various checked
     * exceptions thrown by {@code Future.get()} are wrapped into
     * {@code IOException} exceptions.
     *
     * @param future the future to wait for
     * @param <T>    the result type of the future
     * @return the result produced by the future
     * @throws IOException if the future failed or waiting was interrupted
     */
    public static <T> T waitFor(CompletableFuture<? extends T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // reset interrupted flag
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw wrapInIOException(e.getCause());
        }
    }

    /**
     * Returns a {@code ResponseProcessor} that checks whether a request was
     * successful based on a given predicate. The resulting processor invokes
     * the given predicate on the response received from the server. If the
     * predicate yields <strong>false</strong>, an {@code IOException} is
     * thrown indicating a failed request. Otherwise, the original
     * {@code ResponseProcessor} is invoked, which can now safely generate its
     * result.
     *
     * @param processor        the {@code ResponseProcessor} to wrap
     * @param successPredicate a predicate to determine whether the response is
     *                         successful
     * @param <T>              the result type of the {@code ResponseProcessor}
     * @return the {@code ResponseProcessor} checking the response
     */
    public static <T> ResponseProcessor<T> checkResponse(ResponseProcessor<T> processor,
                                                         Predicate<Response> successPredicate) {
        return response -> {
            if (!successPredicate.test(response)) {
                String msg = String.format(Locale.ROOT,
                        "Response with status %d failed success check.", response.statusCode());
                throw new IOException(msg);
            }
            return processor.process(response);
        };
    }

    /***
     * Returns a {@code ResponseProcessor} that checks the HTTP status code to
     * determine whether a request was successful. This method is equivalent to
     * the overloaded {@code checkResponse()} method using
     * {@link #SUCCESS_STATUS} as predicate.
     * @param processor the {@code ResponseProcessor} to wrap
     * @param <T> the result type of the {@code ResponseProcessor}
     * @return the {@code ResponseProcessor} checking the response
     */
    public static <T> ResponseProcessor<T> checkResponse(ResponseProcessor<T> processor) {
        return checkResponse(processor, SUCCESS_STATUS);
    }

    /**
     * Returns a predicate that checks a response for a specific status code.
     * The response is considered successful if and only if the status code
     * matches exactly the expected code.
     *
     * @param status the expected status code for the response
     * @return a predicate checking for this response status code
     */
    public static Predicate<Response> hasStatus(int status) {
        return response -> response.statusCode() == status;
    }

    /**
     * Returns a {@code ResponseProcessor} that uses the {@code ObjectMapper}
     * specified to map the JSON payload of a response to an object of the
     * given result class. The resulting processor directly accesses the
     * payload of the response; it can be combined with one of the
     * {@code checkResponse()} methods to make sure that the response is
     * successful before it is processed.
     *
     * @param mapper      the JSON mapper
     * @param resultClass the result class
     * @param <T>         the type of the resulting object
     * @return the {@code ResponseProcessor} doing a JSON de-serialization
     */
    public static <T> ResponseProcessor<T> jsonResult(ObjectMapper mapper, Class<T> resultClass) {
        return response -> mapper.readValue(response.bodyStream(), resultClass);
    }

    /**
     * Returns a {@code ResponseProcessor} that uses the {@code ObjectMapper}
     * specified to map the JSON payload of a response to an object of the
     * type defined by the given reference. This is analogous to the overloaded
     * method, but allows for more flexibility  to specify the result type.
     *
     * @param mapper        the JSON mapper
     * @param typeReference the reference defining the target type
     * @param <T>           the type of the resulting object
     * @return the {@code ResponseProcessor} doing a JSON de-serialization
     */
    public static <T> ResponseProcessor<T> jsonResult(ObjectMapper mapper, TypeReference<T> typeReference) {
        return response -> mapper.readValue(response.bodyStream(), typeReference);
    }

    /**
     * Returns a very simple {@code RequestProducer} that just configures its
     * {@code RequestBuilder} with the given URI. This causes an HTTP GET
     * request to this URI without further properties.
     *
     * @param uri the URI to be retrieved
     * @return the {@code RequestProducer} generating this GET request
     */
    public static RequestProducer get(String uri) {
        return builder -> builder.uri(uri);
    }

    /**
     * Wraps the given exception into an {@code IOException} if necessary. If
     * the exception is already an {@code IOException}, it is returned
     * directly.
     *
     * @param e the exception to be wrapped
     * @return the IOException wrapping the exception
     */
    private static IOException wrapInIOException(Throwable e) {
        return e instanceof IOException ? (IOException) e :
                new IOException(e);
    }
}
