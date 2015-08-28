package support;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * @author:xingchaowang
 * @date: 3/12/2015.
 */
public class MysqlDbServer implements SmartLifecycle {
    private final Logger logger = LoggerFactory.getLogger(HyperSqlDbServer.class);

    MysqldResource mysqldResource;
    boolean running = false;

    private int port;
    private String userName;
    private String password;
    private String dbName = "zeus_test";

    private String driver = "com.mysql.jdbc.Driver";
    private String tablesSqlFile = "/sql/create-tables.sql";

    public MysqlDbServer() {

        File databaseDir = new File(System.getProperty("mysql.test-dir", "target/temp/mysql-mxj"));
        if (!databaseDir.exists()) {
            databaseDir.mkdirs();
        }

        port = Integer.parseInt(System.getProperty("mysql.test-port", "3336"));
        userName = "root";
        password = "";

        mysqldResource = startDatabase(databaseDir, port, userName, password);

    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        logger.info("Starting DB server...");
        try {
            createTables();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        running = true;
    }

    @Override
    public void stop() {
        logger.info("Stopping DB server...");
        try {
            mysqldResource.shutdown();
            running = false;
            if (!mysqldResource.isRunning()) {
                FileUtils.forceDelete(mysqldResource.getBaseDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        stop();
        runnable.run();
    }

    public static MysqldResource startDatabase(File databaseDir, int port, String userName, String password) {
        MysqldResource mysqldResource = new MysqldResource(databaseDir);
        Map database_options = new HashMap();
        database_options.put(MysqldResourceI.PORT, Integer.toString(port));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, userName);
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, "root");
        mysqldResource.start("test-mysqld-thread" + new Random().nextInt(), database_options);
        if (!mysqldResource.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }
        System.out.println("MySQL is running.");
        return mysqldResource;
    }

    private void createTables() throws ClassNotFoundException, SQLException, IOException {
        Class.forName(driver);
        Connection conn = null;
        try {
            String url = "jdbc:mysql://localhost:" + port + "/" + dbName
                    + "?" + "createDatabaseIfNotExist=true";
            conn = DriverManager.getConnection(url, userName, "root");

            runScript(conn, new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(tablesSqlFile))));

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void runScript(Connection conn, Reader reader) throws IOException,
            SQLException {

        boolean autoCommit = false;
        boolean fullLineDelimiter = false;
        boolean stopOnError = true;
        String delimiter = ";";

        conn.setAutoCommit(autoCommit);

        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
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
                            for (int i = 0; i < cols; i++) {
                                String name = md.getColumnLabel(i);
                            }
                            while (rs.next()) {
                                for (int i = 0; i < cols; i++) {
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
            conn.rollback();
        }
    }


    public static void main(String[] args) {
        InputStream in = MysqlDbServer.class.getResourceAsStream("/sql/create-tables.sql");
        Scanner scanner = new Scanner(in).useDelimiter("\\A");
        System.out.println(scanner.hasNext() ? scanner.next() : "");
    }
}
