/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.asto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Async {@link java.util.function.Supplier} of {@link java.util.concurrent.CompletionStage}
 * with {@link Content}. It's a {@link FunctionalInterface}.
 *
 * @since 0.25
 */
@FunctionalInterface
public interface AsyncContent extends Supplier<CompletionStage<? extends Content>> {

    /**
     * Empty async content.
     */
    AsyncContent EMPTY = () -> CompletableFuture.completedFuture(Content.EMPTY);

    @Override
    CompletionStage<? extends Content> get();

    /**
     * Failed async content.
     * @since 0.30
     */
    final class Failed implements AsyncContent {

        /**
         * Failure cause.
         */
        private final Throwable reason;

        /**
         * Ctor.
         * @param reason Failure cause
         */
        public Failed(final Throwable reason) {
            this.reason = reason;
        }

        @Override
        public CompletionStage<? extends Content> get() {
            final CompletableFuture<? extends Content> res = new CompletableFuture<>();
            res.completeExceptionally(this.reason);
            return res;
        }
    }
}
