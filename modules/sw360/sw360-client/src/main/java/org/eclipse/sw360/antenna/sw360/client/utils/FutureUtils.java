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
package org.eclipse.sw360.antenna.sw360.client.utils;

import org.eclipse.sw360.antenna.http.utils.FailedRequestException;
import org.eclipse.sw360.antenna.http.utils.HttpConstants;
import org.eclipse.sw360.antenna.http.utils.HttpUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>
 * A class with utility methods related to completable futures and error
 * handling in asynchronous calls.
 * </p>
 */
public class FutureUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private FutureUtils() {
    }

    /**
     * Blocks until the future specified has completed and returns the result.
     * This method is used by the adapter classes to map asynchronous client
     * methods to blocking calls. It works like the
     * {@link HttpUtils#waitFor(CompletableFuture)} method, but also wraps
     * checked {@code IOException} exceptions into runtime exceptions.
     *
     * @param future the future to block for
     * @param <T>    the result type of the future
     * @return the result of the future if it completed successfully
     * @throws SW360ClientException if the future failed with a checked
     *                              exception
     */
    public static <T> T block(CompletableFuture<? extends T> future) {
        try {
            return HttpUtils.waitFor(future);
        } catch (IOException e) {
            throw new SW360ClientException("Asynchronous call failed.", e);
        }
    }

    /**
     * Applies a callback to a future that becomes effective if and only if the
     * future fails with a specific exception. The exception to trigger the
     * callback is defined by a predicate passed to this method. If the future
     * fails with an exception matched by the predicate, another future is
     * returned that is obtained from the {@code fallback} supplier. Here an
     * alternative result value could be computed. If the future was successful
     * or failed with a different exception, the original result is passed to
     * the caller.
     *
     * @param future    the future to be decorated with a fallback
     * @param condition the predicate when to apply the fallback
     * @param fallback  a supplier for the fallback future
     * @param <T>       the result type of the future
     * @return the future with the conditional fallback
     */
    public static <T> CompletableFuture<T> wrapFutureForConditionalFallback(CompletableFuture<T> future,
                                                                            Predicate<? super Throwable> condition,
                                                                            Supplier<? extends CompletableFuture<T>> fallback) {
        return future.handle((result, exception) ->
                exceptionMatches(exception, condition) ?
                        Optional.<CompletableFuture<T>>empty() :
                        Optional.of(future))
                .thenCompose(optFuture -> optFuture.orElseGet(fallback));
    }

    /**
     * Maps the given future to a new future that returns an empty
     * {@code Optional} if the original future completes with an exception
     * indicating a 404 NOT FOUND failure. If the future completes normally,
     * the resulting future completes with a corresponding defined
     * {@code Optional}. In all other cases, the resulting future completes
     * with the same exception as the original future. This method is used by
     * adapter classes to map specific failed REST calls on the client layer to
     * optional results.
     *
     * @param future the future to be decorated
     * @param <T>    the result type of the future
     * @return a future returning an optional value
     */
    public static <T> CompletableFuture<Optional<T>> optionalFuture(CompletableFuture<? extends T> future) {
        CompletableFuture<Optional<T>> optFuture = future.thenApply(Optional::of);
        return wrapFutureForConditionalFallback(optFuture,
                FutureUtils::resourceNotFound,
                () -> CompletableFuture.completedFuture(Optional.empty()));
    }

    /**
     * Tests whether the given exception is a {@link FailedRequestException}
     * with the passed in status code.
     *
     * @param exception  the exception to be checked
     * @param statusCode the status code
     * @return <strong>true</strong> if the exception indicates a failed
     * request with this status code; <strong>false</strong> otherwise
     */
    public static boolean isFailedRequestWithStatus(Throwable exception, int statusCode) {
        return exception instanceof FailedRequestException &&
                ((FailedRequestException) exception).getStatusCode() == statusCode;
    }

    /**
     * Checks whether the passed in exception fulfills the given predicate. If
     * the exception is not <strong>null</strong>, it is unwrapped and passed
     * to the predicate.
     *
     * @param exception the exception to be checked
     * @param condition the predicate to check the exception
     * @return a flag whether the exception is not <strong>null</strong> and is
     * matched by the predicate
     */
    private static boolean exceptionMatches(Throwable exception, Predicate<? super Throwable> condition) {
        return exception != null && condition.test(HttpUtils.unwrapCompletionException(exception));
    }

    /**
     * Checks whether the given exception represents a request that failed with
     * a 404 NOT FOUND status.
     *
     * @param exception the exception to be checked
     * @return a flag whether the exception is a 404 request
     */
    private static boolean resourceNotFound(Throwable exception) {
        return isFailedRequestWithStatus(exception, HttpConstants.STATUS_ERR_NOT_FOUND);
    }
}
