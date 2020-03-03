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
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FutureUtilsTest {
    /**
     * A defined result used by test futures.
     */
    private static final Integer RESULT = 42;

    /**
     * Checks that the given future has failed with the passed in exception.
     *
     * @param future    the future to be checked
     * @param exception the expected exception
     */
    private static void expectFailedFuture(CompletableFuture<?> future, Throwable exception) {
        try {
            FutureUtils.block(future);
            fail("No exception thrown!");
        } catch (SW360ClientException e) {
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void testBlockSuccessfulFuture() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(RESULT);

        assertThat(FutureUtils.block(future)).isEqualTo(RESULT);
    }

    @Test
    public void testBlockFailedFuture() {
        IOException exception = new IOException("Failed future");
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.completeExceptionally(exception);

        expectFailedFuture(future, exception);
    }

    @Test
    public void testOptionalFutureSuccess() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(RESULT);

        CompletableFuture<Optional<Integer>> optFuture = FutureUtils.optionalFuture(future);
        assertThat(FutureUtils.block(optFuture)).contains(RESULT);
    }

    @Test
    public void testOptionalFutureFailure() {
        FailedRequestException exception = new FailedRequestException("a tag", 500);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.completeExceptionally(exception);

        CompletableFuture<Optional<Integer>> optFuture = FutureUtils.optionalFuture(future);
        expectFailedFuture(optFuture, exception);
    }

    @Test
    public void testOptionalFutureWithNotFoundFailure() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.completeExceptionally(new FailedRequestException("tag", HttpConstants.STATUS_ERR_NOT_FOUND));

        CompletableFuture<Optional<Integer>> optFuture = FutureUtils.optionalFuture(future);
        assertThat(FutureUtils.block(optFuture)).isNotPresent();
    }
}
