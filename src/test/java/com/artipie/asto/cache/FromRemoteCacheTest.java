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
package com.artipie.asto.cache;

import com.artipie.asto.AsyncContent;
import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FromRemoteCache}.
 * @since 0.30
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class FromRemoteCacheTest {

    /**
     * Test storage.
     */
    private Storage storage;

    /**
     * Test cache.
     */
    private Cache cache;

    @BeforeEach
    void setUp() {
        this.storage = new InMemoryStorage();
        this.cache = new FromRemoteCache(this.storage);
    }

    @Test
    void obtainsItemFromRemoteAndCaches() {
        final byte[] content = "123".getBytes();
        final Key key = new Key.From("item");
        MatcherAssert.assertThat(
            "Returns content from remote",
            new PublisherAs(
                this.cache.load(
                    key,
                    () -> CompletableFuture.completedFuture(new Content.From(content)),
                    CacheControl.Standard.ALWAYS
                ).toCompletableFuture().join()
            ).bytes().toCompletableFuture().join(),
            new IsEqual<>(content)
        );
        MatcherAssert.assertThat(
            "Saves to storage",
            new PublisherAs(this.storage.value(key).join()).bytes().toCompletableFuture().join(),
            new IsEqual<>(content)
        );
    }

    @Test
    void loadsFromCacheWhenObtainFromRemoteFailed() {
        final byte[] content = "098".getBytes();
        final Key key = new Key.From("some");
        this.storage.save(key, new Content.From(content)).join();
        MatcherAssert.assertThat(
            "Returns content from storage",
            new PublisherAs(
                this.cache.load(
                    key,
                    new AsyncContent.Failed(new IOException("IO error")),
                    CacheControl.Standard.ALWAYS
                ).toCompletableFuture().join()
            ).bytes().toCompletableFuture().join(),
            new IsEqual<>(content)
        );
    }

    @Test
    void failsIfRemoteNotAvailableAndItemIsNotValid() {
        final Key key = new Key.From("any");
        this.storage.save(key, Content.EMPTY).join();
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                CompletionException.class,
                () -> this.cache.load(
                    key,
                    new AsyncContent.Failed(new ConnectException("Not available")),
                    CacheControl.Standard.NO_CACHE
                ).toCompletableFuture().join()
            ).getCause(),
            new IsInstanceOf(ConnectException.class)
        );
    }

}
