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
package org.blocks4j.reconf.client.setup;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.infra.shutdown.ShutdownInterceptor;
import org.blocks4j.reconf.infra.system.FileSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;


public class DatabaseManager implements ShutdownBean {

    private static final String DB_VERSION = "v4";
    private static final MessagesBundle msg = MessagesBundle.getBundle(DatabaseManager.class);
    private final File directory;
    private final BasicDataSource dataSource;
    private boolean init;

    private final String TEMPORARY_INSERT = "INSERT INTO PUBLIC.CLS_METHOD_PROP_VALUE (NAM_CLASS, NAM_METHOD, FULL_PROP, NEW_VALUE, UPDATED) VALUES (?,?,?,?,?)";
    private final String TEMPORARY_UPDATE = "UPDATE PUBLIC.CLS_METHOD_PROP_VALUE SET NEW_VALUE = ?, UPDATED = ? WHERE FULL_PROP = ? AND NAM_CLASS = ? AND NAM_METHOD = ? AND (UPDATED IS NULL OR VALUE <> ?)";
    private final String COMMIT_TEMP_CHANGES = "UPDATE PUBLIC.CLS_METHOD_PROP_VALUE SET VALUE = NEW_VALUE, NEW_VALUE = NULL, UPDATED = ? WHERE FULL_PROP IN (%s) AND NAM_CLASS = ? AND NEW_VALUE IS NOT NULL";
    private final String CLEAN_OLD_TEMP = "UPDATE PUBLIC.CLS_METHOD_PROP_VALUE SET NEW_VALUE = NULL, UPDATED = NULL";
    private final String CHECK_IS_NEW = "SELECT 1 FROM PUBLIC.CLS_METHOD_PROP_VALUE WHERE FULL_PROP = ? AND NAM_CLASS = ? AND NAM_METHOD = ? AND (UPDATED IS NULL OR VALUE <> ?)";

    public DatabaseManager(LocalCacheSettings config) {
        new ShutdownInterceptor(this).register();
        try {
            if (null == config) {
                throw new ReConfInitializationError(msg.get("error.dir.not.provided"));
            }

            this.directory = new File(config.getBackupLocation().getPath() + FileSeparator.value() + DB_VERSION);
            provisionBackupDirectory();

            DatabaseURL url = DatabaseURL.location(directory.getPath()).encrypted();
            if (config.isCompressed()) {
                url = url.compressed();
            }
            if (config.getMaxLogFileSize() > 0) {
                url = url.maxLogFileSize(config.getMaxLogFileSize());
            }

            firstConnection(url);
            dataSource = createDataSource(url);
            if (!tableExists()) {
                createTable();
            } else {
                cleanTable();
            }

            init = true;
        } catch (Throwable t) {
            throw new ReConfInitializationError(t);
        }
    }

    private void provisionBackupDirectory() {
        LoggerHolder.getLog().info(msg.format("setup.local.dir", directory));
        if (directory.isFile()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.file", directory));
        }
        if (!directory.exists()) {
            LoggerHolder.getLog().info(msg.format("local.dir.not.found", directory));
            try {
                FileUtils.forceMkdir(directory);
            } catch (Exception e) {
                throw new ReConfInitializationError(e);
            }
            LoggerHolder.getLog().info(msg.format("local.dir.new", directory));
        }
        if (!directory.canRead()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.read", directory));
        }
        if (!directory.canWrite()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.write", directory));
        }
        File parent = directory.getParentFile();
        if (!parent.canRead()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.read", parent));
        }
        if (!parent.canWrite()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.write", parent));
        }
    }

    private boolean tableExists() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute("SELECT 1                                 " +
                         "FROM   INFORMATION_SCHEMA.TABLES         " +
                         "WHERE  TABLE_CATALOG = 'PUBLIC'          " +
                         "AND    TABLE_SCHEMA = 'PUBLIC'           " +
                         "AND    TABLE_NAME='CLS_METHOD_PROP_VALUE'");
            return stmt.getResultSet().next();

        } finally {
            close(stmt);
            close(conn);
        }
    }

    public String get(String fullProperty, Method method) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT VALUE                        " +
                                         "FROM   PUBLIC.CLS_METHOD_PROP_VALUE " +
                                         "WHERE  FULL_PROP = ?                " +
                                         "AND    NAM_CLASS = ?                " +
                                         "AND    NAM_METHOD = ?               ");

            stmt.setString(1, StringUtils.upperCase(fullProperty));
            stmt.setString(2, method.getDeclaringClass().getName());
            stmt.setString(3, method.getName());
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getClob(1) == null ? null : rs.getClob(1).getCharacterStream() == null ? null : IOUtils.toString(rs.getClob(1).getCharacterStream());
            }

            return null;

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "get"), e);
            throw new RuntimeException(e);

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }

    public boolean temporaryUpsert(String fullProperty, Method method, String value) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            if (needToInsert(fullProperty, method)) {
                stmt = conn.prepareStatement(TEMPORARY_INSERT);
                stmt.setString(1, method.getDeclaringClass().getName());
                stmt.setString(2, method.getName());
                stmt.setString(3, StringUtils.upperCase(fullProperty));
                stmt.setString(4, value);
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            } else {
                stmt = conn.prepareStatement(TEMPORARY_UPDATE);
                stmt.setString(1, value);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, StringUtils.upperCase(fullProperty));
                stmt.setString(4, method.getDeclaringClass().getName());
                stmt.setString(5, method.getName());
                stmt.setString(6, value);
            }

            boolean result = 0 != stmt.executeUpdate();
            return result;

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", ("temporaryUpsert")), e);
            throw new RuntimeException(e);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    public boolean isNew(String fullProperty, Method method, String value) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            if (needToInsert(fullProperty, method)) {
                return true;
            }

            stmt = conn.prepareStatement(CHECK_IS_NEW);
            stmt.setString(1, StringUtils.upperCase(fullProperty));
            stmt.setString(2, method.getDeclaringClass().getName());
            stmt.setString(3, method.getName());
            stmt.setString(4, value);

            rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", ("isNew")), e);
            throw new RuntimeException(e);

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }

    private boolean needToInsert(String fullProperty, Method method) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT 1                            " +
                                         "FROM   PUBLIC.CLS_METHOD_PROP_VALUE " +
                                         "WHERE  FULL_PROP = ?                " +
                                         "AND    NAM_CLASS = ?                " +
                                         "AND    NAM_METHOD = ?               ");

            stmt.setString(1, StringUtils.upperCase(fullProperty));
            stmt.setString(2, method.getDeclaringClass().getName());
            stmt.setString(3, method.getName());
            rs = stmt.executeQuery();

            return rs.next() ? false : true;

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "needToInsert"), e);
            throw new RuntimeException(e);

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }


    public void commitTemporaryUpdate(Collection<String> fullProperties, Class<?> declaringClass) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String qry = String.format(COMMIT_TEMP_CHANGES, StringUtils.join(toUpper(fullProperties), ","));

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(qry);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, declaringClass.getName());
            int changed = stmt.executeUpdate();
            LoggerHolder.getLog().debug(msg.format("db.update.number", changed));

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "commitTemporaryUpdate"), e);
            throw new RuntimeException(e);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    private Collection<String> toUpper(Collection<String> arg) {
        Set<String> result = new LinkedHashSet<String>();
        for (String str : arg) {
            result.add("'" + StringUtils.upperCase(str) + "'");
        }
        return result;
    }

    private void createTable() throws Exception {
        execute("CREATE TABLE PUBLIC.CLS_METHOD_PROP_VALUE        " +
                "(NAM_CLASS VARCHAR(255) NOT NULL,                " +
                " NAM_METHOD VARCHAR(255) NOT NULL,               " +
                " FULL_PROP LONGVARCHAR NOT NULL,                 " +
                " VALUE LONGVARCHAR,                              " +
                " NEW_VALUE LONGVARCHAR,                          " +
                " UPDATED TIMESTAMP,                              " +
                " PRIMARY KEY (NAM_CLASS, NAM_METHOD, FULL_PROP)) ");
    }

    private void cleanTable() throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(CLEAN_OLD_TEMP);
            stmt.executeUpdate();

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "cleanTable"), e);
            throw new RuntimeException(e);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    private void execute(String cmd) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(cmd);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    public static void close(Statement arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception ignored) { }
    }

    public static void close(ResultSet arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception ignored) { }
    }

    public static void close(Connection arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception ignored) { }
    }

    private Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new RuntimeException(msg.get("error.datasource.null"));
        }
        Connection conn = dataSource.getConnection();
        if (conn == null) {
            throw new RuntimeException(msg.get("error.connection.null"));
        }
        conn.setAutoCommit(true);
        return conn;
    }

    private void firstConnection(DatabaseURL arg) throws Exception {

        Connection conn = null;
        try {
            Class.forName(arg.getDriverClassName());
            conn = DriverManager.getConnection(arg.buildInitalURL(), arg.getLogin(), arg.getPass());

        } finally {
            close(conn);
        }
    }

    private BasicDataSource createDataSource(DatabaseURL arg) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(arg.getDriverClassName());
        ds.setUrl(arg.buildRuntimeURL());
        ds.setUsername(arg.getLogin());
        ds.setPassword(arg.getPass());
        ds.setMaxActive(30);
        ds.setMinIdle(5);
        ds.setTestOnBorrow(true);
        ds.setMaxWait(5000);
        return ds;
    }

    public void shutdown() {
        if (!init) {
            return;
        }
        try {
            LoggerHolder.getLog().info(msg.get("db.stopping"));
            if (dataSource != null) {
                execute("SHUTDOWN");
                dataSource.close();
            }
            LoggerHolder.getLog().info(msg.get("db.stopped"));
        } catch (Exception ignored) {
            LoggerHolder.getLog().warn(msg.get("error.db.stopping"), ignored);
        }
    }
}
