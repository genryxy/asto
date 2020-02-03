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

import com.artipie.asto.fs.RxFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RxFile}.
 * @since 0.11.1
 */
public class RxFileTest {

    /**
     * Test that {@link RxFile#flow()} works.
     * @throws IOException if fails
     */
    @Test
    public void rxFileFlowWorks() throws IOException {
        final String hello = "hello-world";
        final Path temp = Files.createTempFile(hello, ".txt");
        Files.write(temp, hello.getBytes());
        final String content = new RxFile(temp)
            .flow()
            .rebatchRequests(1)
            .toList()
            .map(bytes -> new String(new ByteArray(bytes).primitiveBytes()))
            .blockingGet();
        MatcherAssert.assertThat(hello, Matchers.equalTo(content));
        temp.toFile().deleteOnExit();
    }
}
