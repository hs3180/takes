/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Yegor Bugayenko
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
package org.takes.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An InputStream with a timeout.
 * @author Shan Huang (thuhuangs09@gmail.com)
 * @version $Id$
 */
class TimeoutInputStream extends FilterInputStream {

    /**
     * Timeout parameter for TimeoutInputStream.
     */
    private long timeout;

    /**
     * Constructor for TimeoutInputStream.
     * @param input InputStream to be wrapped.
     * @param inittimeout Initial timeout in milliseconds
     */
    TimeoutInputStream(final InputStream input, final long inittimeout) {
        super(input);
        this.timeout = inittimeout;
    }

    /**
     * Timeout setter.
     * @param newtimeout New timeout to be set in milliseconds.
     */
    public void setTimeout(final long newtimeout) {
        this.timeout = newtimeout;
    }

    /**
     * Timeout getter.
     * @return Current timeout in milliseconds.
     */
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public int read() throws IOException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Integer> future = executor.submit(new ReadTask(this.in));
        int ret = -1;
        boolean isTimeout = false;
        try {
            ret = future.get(this.timeout, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            throw new IOException(ex);
        } catch (final ExecutionException ex) {
            throw new IOException(ex);
        } catch (final TimeoutException ex) {
            isTimeout = true;
        }
        if (isTimeout) {
            throw new SocketTimeoutException();
        }
        return ret;
    }

    class ReadTask implements Callable<Integer> {

        /**
         * InputStream to be read.
         */
        private final transient InputStream input;

        /**
         * Constructor of ReadTask.
         * @param inputstream Target InputStream.
         */
        ReadTask(final InputStream inputstream) {
            this.input = inputstream;
        }

        @Override
        public Integer call() throws IOException {
            return this.input.read();
        }
    }
}
