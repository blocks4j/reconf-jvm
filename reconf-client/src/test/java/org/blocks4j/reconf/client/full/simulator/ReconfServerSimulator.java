package org.blocks4j.reconf.client.full.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReconfServerSimulator implements HttpRequestHandler {

    private static final Pattern EXTRACTION_PATTERN = Pattern.compile("^([^?]*).*");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ReconfServerMemoryDatabase memoryDatabase;


    public ReconfServerSimulator(ReconfServerMemoryDatabase memoryDatabase) {
        this.memoryDatabase = memoryDatabase;
    }

    public ReconfServerSimulator(URL memoryDatabaseJsonLocation) throws IOException {
        this.memoryDatabase = OBJECT_MAPPER.readValue(memoryDatabaseJsonLocation, ReconfServerMemoryDatabase.class);
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws IOException {
        String extractKey = this.extractKey(httpRequest);

        String value = this.memoryDatabase.getPropertyValue(extractKey);

        httpResponse.setEntity(new StringEntity(value));

    }

    private String extractKey(HttpRequest httpRequest) {
        Matcher matcher = EXTRACTION_PATTERN.matcher(httpRequest.getRequestLine().getUri());

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
