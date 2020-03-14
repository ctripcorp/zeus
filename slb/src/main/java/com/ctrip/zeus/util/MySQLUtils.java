package com.ctrip.zeus.util;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @Discription
 **/
@Component
public class MySQLUtils {

    @Resource
    private DataSource dataSource;

    public void runScript(Connection conn, Reader fileReader) throws IOException, SQLException {
        if (conn == null) {
            conn = dataSource.getConnection();
        }

        boolean autoCommit = false;
        boolean fullLineDelimiter = false;
        boolean stopOnError = true;
        String delimiter = ";";

        conn.setAutoCommit(autoCommit);

        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(fileReader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    //Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else {
                    if (!fullLineDelimiter
                            && trimmedLine.endsWith(delimiter)
                            || fullLineDelimiter
                            && trimmedLine.equals(delimiter)) {
                        command.append(line.substring(0, line
                                .lastIndexOf(delimiter)));
                        command.append(" ");
                        Statement statement = conn.createStatement();

                        boolean hasResults = false;
                        if (stopOnError) {
                            hasResults = statement.execute(command.toString());
                        } else {
                            try {
                                statement.execute(command.toString());
                            } catch (SQLException e) {
                                e.fillInStackTrace();
                                //Do nothing
                            }
                        }

                        if (autoCommit && !conn.getAutoCommit()) {
                            conn.commit();
                        }

                        ResultSet rs = statement.getResultSet();
                        if (hasResults && rs != null) {
                            ResultSetMetaData md = rs.getMetaData();
                            int cols = md.getColumnCount();
                            for (int i = 1; i <= cols; i++) {
                                String name = md.getColumnLabel(i);
                            }
                            while (rs.next()) {
                                // column index of ResultSet starts from 1, 2, ...
                                for (int i = 1; i <= cols; i++) {
                                    String value = rs.getString(i);
                                }
                            }
                        }

                        command = null;
                        try {
                            statement.close();
                        } catch (Exception e) {
                            // Ignore to workaround a bug in Jakarta DBCP
                        }
                        Thread.yield();
                    } else {
                        command.append(line);
                        command.append(" ");
                    }
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    public Set<String> getTables(Connection connection) throws Exception {
        if (connection == null) {
            connection = dataSource.getConnection();
        }
        try {
            Set<String> result = new HashSet<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", null);
            while (rs.next()) {
                result.add(rs.getString(3).trim());
            }

            return result;
        } finally {
            connection.close();
        }

    }
}
