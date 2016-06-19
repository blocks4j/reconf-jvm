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
package org.blocks4j.reconf.infra.http;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.system.LocalHostname;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class ReconfServerStub implements ReconfServer {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ReconfServerStub.class);
    private static final String PROTOCOL = "reconf.client-v1+text/plain";
    private final String serviceUri;
    private String instance;

    private CloseableHttpClient httpClient;

    public ReconfServerStub(String serviceUri, long timeout, TimeUnit timeUnit, int maxRetry) {
        this.serviceUri = serviceUri;
        this.instance = LocalHostname.getName();

        this.httpClient = this.createHttpClient(timeout, timeUnit, maxRetry);
    }

    private CloseableHttpClient createHttpClient(long timeout, TimeUnit timeUnit, int maxRetry) {
        int timeMillis = (int) TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return HttpClientBuilder.create()
                .setRetryHandler(new RetryHandler(maxRetry))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .setConnectTimeout(timeMillis)
                        .setSocketTimeout(timeMillis)
                        .setConnectionRequestTimeout(timeMillis)
                        .build())
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(50)
                .build();

    }

    @Override
    public String get(String product, String component, String property) throws Exception {
        final URIBuilder baseURIBuilder = this.createBaseURIBuilder(serviceUri, product, component, property)
                .setParameter("instance", instance);

        URI requestUri = baseURIBuilder.build();
        final HttpGet request = new HttpGet(requestUri);
        request.addHeader("Accept-Encoding", "gzip,deflate");
        request.addHeader("X-ReConf-Protocol", PROTOCOL);

        int status = 0;
        try (CloseableHttpResponse httpResponse = this.httpClient.execute(request)) {
            status = httpResponse.getStatusLine().getStatusCode();
            if (status == 200) {
                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            if (status == 0) {
                throw new IllegalStateException(msg.format("error.generic", requestUri.toString()), e);
            }
            throw new IllegalStateException(msg.format("error.http", status, requestUri.toString()), e);
        }

        throw new IllegalStateException(msg.format("error.http", status, requestUri.toString()));
    }

    private URIBuilder createBaseURIBuilder(String serviceUri, String product, String component, String property) {
        try {
            URIBuilder baseBuilder = new URIBuilder(serviceUri);
            if (baseBuilder.getScheme() == null) {
                baseBuilder = new URIBuilder("http://" + serviceUri);
            }

            baseBuilder.setPath(String.format("/%s/%s/%s", product, component, property));

            return baseBuilder;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            throw new IllegalStateException(msg.format("error.http.client.shutdown"));
        }
    }
}
