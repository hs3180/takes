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

package org.takes.it.fm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class for persistent connection testing.
 * @author Shan Huang (thuhuangs09@gmail.com)
 * @version $Id$
 */
public final class KeepAliveITCase {

    /**
     * Address for takes server.
     */
    private static final String HOME = System.getProperty("takes.home");
    /**
     * Http seperate symbol.
     */
    private static final String CRLF = "\r\n";

    /**
     * Create a socket, try to download about page twice using the same socket.
     * @throws IOException Socket IOException
     */
    @Test
    public void tryKeepAliveConnection() throws IOException {
        final String pageAddr = HOME;
        final URL url = new URL(pageAddr);
        final String host = url.getHost();
        final String file = "/about";
        final Socket clientSocket = new Socket(host, url.getPort());
        this.download(clientSocket, host, file);
        this.download(clientSocket, host, file);
        clientSocket.close();
    }

    /**
     * Download file from host using given socket.
     * @param socket Socket for downloading connection
     * @param host Target hostname
     * @param file Specified file to be downloaded
     * @throws IOException Socket IO exception
     */
    private void download(
        final Socket socket,
        final String host,
        final String file
    ) throws IOException {
        final BufferedReader inFromServer =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
        final OutputStreamWriter outWriter =
            new OutputStreamWriter(socket.getOutputStream());
        outWriter.write(
            "GET " + file + " HTTP/1.1" + CRLF
            + "Host: " + host + CRLF
            + CRLF
        );
        outWriter.flush();
        final StringBuffer out = new StringBuffer();
        String input;
        while ((input = inFromServer.readLine()) != null) {
            out.append(input);
        }
        Assert.assertTrue(
            "Couldn't receive 200 response",
            out.toString().startsWith("HTTP/1.1 200 OK")
        );
    }
}
