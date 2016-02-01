package org.blocks4j.reconf.spring.boot.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@ConfigurationProperties(prefix = "endpoints.reconf.sync", ignoreUnknownFields = true)
public class ReconfSyncMvcEndpoint extends AbstractEndpointMvcAdapter<ReconfSyncEndpoint> {

    private static final String BODY_TEMPLATE = "{ \"operation\" : \"%s\", \"result\" : [ %s ] }";

    private String path;

    @Autowired
    public ReconfSyncMvcEndpoint(ReconfSyncEndpoint delegate) {
        super(delegate);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object sync() {
        if (!getDelegate().isEnabled()) {
            return getDisabledResponse();
        }

        return String.format(BODY_TEMPLATE, "sync", this.getDelegate().invoke());
    }

    @Override
    public String getPath() {
        return (this.path != null ? this.path : this.getDefaultPath());
    }

    private String getDefaultPath() {
        return "/" + this.getDelegate().getId().replaceAll("_", "/");
    }

    @Override
    public void setPath(String path) {
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/reconf/" + path;
        }

        this.path = path;
    }
}
