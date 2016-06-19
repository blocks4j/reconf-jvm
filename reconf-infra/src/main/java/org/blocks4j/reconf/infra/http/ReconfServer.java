package org.blocks4j.reconf.infra.http;

import org.blocks4j.reconf.infra.shutdown.ShutdownBean;

public interface ReconfServer extends ShutdownBean {

    String get(String product, String component, String property) throws Exception;

}
