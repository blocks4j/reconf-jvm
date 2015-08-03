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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.utils.URIBuilder;

public class SimpleHttpRequest extends HttpEntityEnclosingRequestBase {

    private final String httpMethod;
    private int queryParams = -1;

    SimpleHttpRequest(String method, String pathBase, String... pathParam) throws URISyntaxException  {
        this.httpMethod = method;

        URIBuilder baseBuilder = new URIBuilder(pathBase);
        if (baseBuilder.getScheme() == null) {
            baseBuilder = new URIBuilder("http://" + pathBase);
        }

        final StringBuilder pathBuilder = new StringBuilder(baseBuilder.getPath());
        for (String param : pathParam) {
            pathBuilder.append("/").append(param);
        }

        this.setURI(new URI(baseBuilder.getScheme(), baseBuilder.getUserInfo(), baseBuilder.getHost(), baseBuilder.getPort(), pathBuilder.toString(), null, null));
    }

    /**
     * Adds a query parameter in the format name=value to this request URI.
     * @param paramName The name of the query parameter
     * @param paramValue The value of the query parameter
     * @return this request updated
     */
    public SimpleHttpRequest addQueryParam(String paramName, String paramValue) {
        final String newUri;
        if (++queryParams > 0) {
            newUri = String.format("%s&%s=%s", this.getURI().toASCIIString(), paramName, this.encode(paramValue));
        } else {
            newUri = String.format("%s?%s=%s", this.getURI().toASCIIString(), paramName, this.encode(paramValue));
        }

        this.setURI(URI.create(newUri));

        return this;
    }

    /**
     * Adds a header to this request.
     * @param name The name of the header
     * @param value The value of the header
     * @return this request updated
     */
    public SimpleHttpRequest addHeaderField(String name, String value) {
        super.addHeader(name, value);
        return this;
    }

    private String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    @Override
    public String getMethod() {
        return this.httpMethod;
    }

}
