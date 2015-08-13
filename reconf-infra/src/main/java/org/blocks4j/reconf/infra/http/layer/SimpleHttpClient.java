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

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.RequestLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

public class SimpleHttpClient {

    private static final MessagesBundle msg = MessagesBundle.getBundle(SimpleHttpClient.class);

    private static ExecutorService requestExecutor = Executors.newFixedThreadPool(20, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "reconf-http-client-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    public static SimpleHttpRequest newGetRequest(String pathBase, String... pathParam) throws URISyntaxException  {
        return new SimpleHttpRequest("GET", pathBase, pathParam);
    }

    public static SimpleHttpResponse defaultExecute(SimpleHttpRequest request, long timeout, TimeUnit timeunit, int retries) throws Exception {
        return execute(newHttpClient(timeout, timeunit, retries), request, timeout, timeunit);
    }

    private static SimpleHttpResponse execute(CloseableHttpClient httpClient, SimpleHttpRequest request, long timeout, TimeUnit timeunit) throws Exception {

        RequestTask task = new RequestTask(httpClient, request);
        Future<SimpleHttpResponse> futureResponse = null;

        try {
            futureResponse = requestExecutor.submit(task);
            return futureResponse.get(timeout, timeunit);

        } catch (TimeoutException e) {
            httpClient.close();
            RequestLine line = request.getRequestLine();
            String method = request.getMethod();

            if (line != null && method != null) {
                throw new TimeoutException(msg.format("error.complete", method.toUpperCase(), line.getUri(), timeout, timeunit.toString().toLowerCase()));

            } else {
                throw new TimeoutException(msg.format("error", timeout, timeunit.toString().toLowerCase()));
            }

        }  catch (Exception e) {
            httpClient.close();
            throw e;

        } finally {
            if (futureResponse != null) {
                futureResponse.cancel(true);
            }
        }
    }

    private static CloseableHttpClient newHttpClient(long timeout, TimeUnit timeUnit, int retries) throws GeneralSecurityException {
        return HttpClientBuilder.create()
                .setRetryHandler(new RetryHandler(retries))
                .setDefaultRequestConfig(createBasicHttpParams(timeout, timeUnit))
                .build();
    }

    private static RequestConfig createBasicHttpParams(long timeout, TimeUnit timeUnit) {
        int timemillis = (int) TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setConnectTimeout(timemillis)
                .setSocketTimeout(timemillis)
                .setConnectionRequestTimeout(timemillis)
                .build();
    }
}