/*
 *   Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.blocks4j.reconf.infra.http.layer;

import java.util.concurrent.Callable;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public class RequestTask implements Callable<SimpleHttpResponse> {

    private final CloseableHttpClient httpClient;
    private final HttpUriRequest request;

    public RequestTask(CloseableHttpClient httpClient, HttpUriRequest request) {
        this.httpClient = httpClient;
        this.request = request;
    }

    public SimpleHttpResponse call() throws Exception {
        return new SimpleHttpResponse(httpClient, request);
    }
}
