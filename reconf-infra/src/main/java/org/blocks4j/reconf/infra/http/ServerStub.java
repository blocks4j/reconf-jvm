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

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.blocks4j.reconf.infra.http.layer.SimpleHttpRequest;
import org.blocks4j.reconf.infra.http.layer.SimpleHttpResponse;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.system.LocalHostname;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class ServerStub {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ServerStub.class);
    private static final String PROTOCOL = "reconf.client-v1+text/plain";
    private final String serviceUri;
    private final long timeout;
    private final TimeUnit timeunit;
    private final int maxRetry;
    private String product;
    private String component;
    private String instance;

    private HttpClient httpClient;

    public ServerStub(String serviceUri, long timeout, TimeUnit timeUnit, int maxRetry) {
        this.serviceUri = serviceUri;
        this.timeout = timeout;
        this.timeunit = timeUnit;
        this.instance = LocalHostname.getName();
        this.maxRetry = maxRetry;

        this.httpClient = this.createHttpClient(timeout, timeUnit, maxRetry);
    }

    private HttpClient createHttpClient(long timeout, TimeUnit timeUnit, int maxRetry) {
        int timeMillis = (int) TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return HttpClientBuilder.create()
                .setRetryHandler(new RetryHandler(maxRetry))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .setConnectTimeout(timeMillis)
                        .setSocketTimeout(timeMillis)
                        .setConnectionRequestTimeout(timeMillis)
                        .build())
                .build();

    }

    public String get(String property) throws Exception {
        final HttpGet httpGet = this.newGetRequest(serviceUri, product, component, property)
                .addQueryParam("instance", instance)
                .addHeaderField("Accept-Encoding", "gzip,deflate")
                .addHeaderField("X-ReConf-Protocol", PROTOCOL);

        int status = 0;
        SimpleHttpResponse result = null;
        try {
            result = factory.execute(httpGet, timeout, timeunit, maxRetry);
            status = result.getStatusCode();
            if (status == 200) {
                return result.getBodyAsString();
            }
        } catch (Exception e) {
            if (status == 0) {
                throw new IllegalStateException(msg.format("error.generic", httpGet.getURI()), e);
            }
            throw new IllegalStateException(msg.format("error.http", status, httpGet.getURI()), e);
        }

        throw new IllegalStateException(msg.format("error.http", result.getStatusCode(), httpGet.getURI()));
    }

    private HttpGet newGetRequest(String serviceUri, String... pathParam) {
        try {
            URIBuilder baseBuilder = new URIBuilder(serviceUri);
            if (baseBuilder.getScheme() == null) {
                baseBuilder = new URIBuilder("http://" + serviceUri);
            }

            final StringBuilder pathBuilder = new StringBuilder(baseBuilder.getPath());
            for (String param : pathParam) {
                pathBuilder.append("/").append(param);
            }

            URI uri = new URI(baseBuilder.getScheme(), baseBuilder.getUserInfo(), baseBuilder.getHost(), baseBuilder.getPort(), pathBuilder.toString(), null, null);

            return new HttpGet(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
