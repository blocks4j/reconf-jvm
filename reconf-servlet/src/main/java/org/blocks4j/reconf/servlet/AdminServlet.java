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
package org.blocks4j.reconf.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * https://github.com/dropwizard/metrics/blob/master/metrics-servlets/src/main/java/com/codahale/metrics/servlets/AdminServlet.java
 */
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String CONTENT_TYPE = "application/json";

    private transient SyncServlet syncServlet;
    private transient String syncUri;


    private static final String BODY_TEMPLATE = "{ \"page\" : { \"title\" : \"%s\", \"href\" : \"%s\" }, \"function\" : [ { \"name\" : \"%s\", \"href\" : \"%s\" } ] }";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.syncServlet = new SyncServlet();
        this.syncServlet.init(config);

        this.syncUri = getParam(config.getInitParameter("sync-uri"), SyncServlet.getDefaultURI());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getContextPath() + req.getServletPath();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        PrintWriter writer = resp.getWriter();

        try {
            resp.setContentType(CONTENT_TYPE);
            writer.println(String.format(BODY_TEMPLATE, "ReConf Operational Menu", path, "sync", path + syncUri));

        } finally {
            writer.close();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);

        } else if (uri.equals(syncUri)) {
            syncServlet.service(req, resp);

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static String getParam(String initParam, String defaultValue) {
        return initParam == null ? defaultValue : initParam;
    }

    public static String getContentType() {
        return CONTENT_TYPE;
    }
}
