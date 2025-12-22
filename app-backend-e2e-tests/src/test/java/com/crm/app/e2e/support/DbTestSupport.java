package com.crm.app.e2e.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public final class DbTestSupport {

    private DbTestSupport() {
    }

    public static void prepareDatabase(DataSource dataSource) {
        runSqlScript(dataSource, "db/prepare.sql");
    }

    public static void runSqlScript(DataSource dataSource, String classpathLocation) {
        try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(classpathLocation));
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (Exception ignored) {
                    // To be ignored
                }
                throw new IllegalStateException("Failed to execute SQL script: " + classpathLocation, e);
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Database preparation failed (script=" + classpathLocation + ")", e);
        }
    }
}
