package com.home.dbimportermaven.misc;

import com.home.dbimportermaven.dbtypes.MetaInfoTable;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handle DB connection pools using the BoneCP framework.
 */
public class DbHandler {
    private static final Logger LOG = LogManager.getLogger(DbHandler.class.getName());

    private DbParametersIF dtp;
    private BoneCP connectionPool;

    /**
     * Establish a connection pool connected to the DB.
     * <p>
     * @param parameters the parameters to use for the JDBC connection
     * <p>
     * @throws Exception in case of any exception
     */
    public DbHandler(DbParametersIF parameters) throws Exception {
        dtp = parameters;
        initJdbc();
    }

    /**
     * Establish a connection pool connected to the DB and give a log message for this.
     * <p>
     * @param parameters  the parameters to use for the JDBC connection
     * @param description the log message
     *
     * @throws Exception in case of any exception
     */
    public DbHandler(DbParametersIF parameters, String description) throws Exception {
        this(parameters);
        LOG.info(description);
    }

    /**
     * Load the DB driver, create a pool of DB connnections and prepare variables for further use.
     * <p>
     * In case of an error throw a runtime exception.
     */
    private void initJdbc() {
        try {
            loadDbDriver();
            LOG.info("Creating ConnectionPool for " + dtp.getDriver() + " now...");
            connectionPool = this.getConnectionPool(10);
            LOG.info("ConnectionPool created");
        }
        catch (SQLException sqlex) {
            LOG.fatal("SQLException: Cannot create ConnectionPool. " + sqlex.getMessage());
            throw new IllegalStateException(sqlex.getMessage());
        }
        catch (Exception ex) {
            LOG.fatal("Exception: Cannot create ConnectionPool. " + ex.getMessage());
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private void loadDbDriver() throws Exception {
        LOG.info("Try to load database driver " + dtp.getDriver());
        Class.forName(dtp.getDriver()).newInstance();
        LOG.info("Database driver loaded...");
    }

    /**
     * Get a BoneCP connection pool for a given amount of connnections.
     *
     * @param maxConnections the max. amount of connnections
     *
     * @return the connnection pool
     *
     * @throws SQLException on any SQL exception
     */
    public BoneCP getConnectionPool(int maxConnections) throws SQLException {
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(dtp.getConnectString());
        config.setUsername(dtp.getUserName());
        config.setPassword(dtp.getPassword());
        config.setMinConnectionsPerPartition(13);
        config.setMaxConnectionsPerPartition(100);
        config.setPartitionCount(3);

        return new BoneCP(config);
    }

    /**
     * Get the pool connection.
     *
     * @return the connection
     *
     * @throws SQLException on any SQL exception
     */
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    private int getTotalNoCreatedConnections() {
        return connectionPool.getTotalCreatedConnections();
    }

    private int getNoFreeConnections() {
        return connectionPool.getTotalFree();
    }

    private int getNoLeasedConnections() {
        return connectionPool.getTotalLeased();
    }

    /**
     * Log the current connnection status in the connection pool.
     */
    public void showConnectionStatus() {
        LOG.debug("=======================================================");
        LOG.debug("Connection: " + dtp.getConnectString());
        LOG.debug("Total: " + this.getTotalNoCreatedConnections());
        LOG.debug("Free: " + this.getNoFreeConnections());
        LOG.debug("Total used: " + this.getNoLeasedConnections());
        LOG.debug("=======================================================");
    }

    /**
     * Select data from a database table.
     * <p>
     * @param con       the DB connection
     * @param selectStr the select string to use for fetching th data
     * @param tableName the table to select from
     *
     * @return the data selected
     *
     * @throws SQLException on any SQL exception
     */
    public LinkedList<LinkedList<String>> getData(Connection con, final String selectStr, String tableName) throws SQLException {
        LinkedList<LinkedList<String>> result = new LinkedList<>();
        getMetaTableInfo(con, tableName);
        Statement stmt = con.createStatement();
        LOG.trace(selectStr);
        ResultSet rset = stmt.executeQuery(selectStr);

        String tmp_Str = selectStr.toUpperCase(Locale.ENGLISH);
        tmp_Str = tmp_Str.substring(tmp_Str.indexOf("SELECT ") + 7, tmp_Str.indexOf(" FROM "));
        String[] col_labels = tmp_Str.split(",");

        while (rset.next()) {
            LinkedList<String> row = new LinkedList<>();
            for (int i = 1; i <= col_labels.length; i++) {
                row.add(rset.getString(i));
            }
            result.add(row);
        }
        LOG.trace("Get Data End");

        return result;
    }

    /**
     * Insert data in a database table.
     * <p>
     * @param con       the DB connection
     * @param data      tha data to insert
     * @param tableName the table to insert into
     *
     * @throws SQLException on any SQL exception
     */
    public void insertData(Connection con, LinkedList<LinkedList<String>> data, final String tableName) throws SQLException {
        LOG.info("Starting data transfer to target table " + tableName);
        MetaInfoTable meta = this.getMetaTableInfo(con, tableName);
        Statement stmt = con.createStatement();
        int row;

        // Start transaction
        con.setAutoCommit(false);
        // Delete table content first
        stmt.executeUpdate("Delete from " + tableName);

        // Insert data per row in a loop
        for (row = 0; row < data.size(); ++row) {
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("Insert into ").append(tableName).append(" (");
            for (int col = 0; col < meta.getColumnCount(); col++) {
                sBuilder.append(meta.getColNames().get(col));
                if (col < meta.getColumnCount() - 1) {
                    sBuilder.append(", ");
                }
                else {
                    sBuilder.append(") VALUES (");
                }
            }
            for (int col = 0; col < meta.getColumnCount(); col++) {
                switch (meta.getColtype(meta.getColNames().get(col))) {
                    case "CHARACTER":
                        sBuilder.append("'").append(data.get(row).get(col).replace("'", " ")).append("'");
                        break;
                    case "TIMESTAMP":
                        sBuilder.append("TIMESTAMP '").append(data.get(row).get(col).replace("'", " ")).append("'");
                        break;
                    default:
                        sBuilder.append(data.get(row).get(col));
                        break;
                }

                if (col < meta.getColumnCount() - 1) {
                    sBuilder.append(", ");
                }
                else {
                    sBuilder.append(")");
                }
            }
            LOG.trace(sBuilder.toString());
            stmt.executeUpdate(sBuilder.toString());
        }
        // Commit the transaction
        con.commit();
        con.setAutoCommit(true);

        LOG.info("Data Transfer to table " + tableName + " completed; [" + row + "] rows");
    }

    /**
     * Generate tables in a database.
     * <p>
     * @param con         the database connection
     * @param DDL_Strings the DDL strings to use
     * @param tableNames  the table to operate on
     *
     * @return false in any case
     *
     * @throws SQLException on any SQL exception
     */
    public boolean generateTables(Connection con, LinkedList<String> DDL_Strings, LinkedList<String> tableNames) throws SQLException {
        Statement stmt = con.createStatement();
        for (int i = 0; i < DDL_Strings.size(); i++) {
            if (this.testTableExists(con, tableNames.get(i))) {
                stmt.executeUpdate("Drop table " + tableNames.get(i));
                LOG.debug("New table " + tableNames.get(i) + " exists. Table dropped.");
            }
            stmt.executeUpdate(DDL_Strings.get(i));
            LOG.debug("New table " + tableNames.get(i) + " created in target destination.");
        }

        return false;
    }

    /**
     * Generate a singel table in a database.
     * <p>
     * @param con       the database connection
     * @param DDLString the DDL string to use
     * @param tableName the table to operate on
     *
     * @return false in any case
     *
     * @throws SQLException on any SQL exception
     */
    public boolean generateSingleTable(Connection con, String DDLString, String tableName) throws SQLException {
        Statement stmt;

        if (testTableExists(con, tableName)) {
            stmt = con.createStatement();
            stmt.executeUpdate("Drop table " + tableName);
            LOG.info("New table " + tableName + " exists. Table dropped.");
        }
        LOG.trace(DDLString);
        stmt = con.createStatement();
        stmt.executeUpdate(DDLString);
        LOG.debug("New table " + tableName + " created in target destination.");

        return false;
    }

    /**
     * Get the SQL meta data for a table.
     * <p>
     * @param con       the database connnection
     * @param tableName the table to fetch metadata from
     *
     * @return the meta infos
     *
     * @throws SQLException on any SQL exception
     */
    public MetaInfoTable getMetaTableInfo(Connection con, String tableName) throws SQLException {
        LOG.debug("Getting metainfo from table " + tableName);

        LinkedList<String> colNames = new LinkedList<>();
        Statement stmt = con.createStatement();
        ResultSet rset;
        int colCount;

        rset = stmt.executeQuery("Select * from " + tableName);
        ResultSetMetaData rsmeta = rset.getMetaData();
        colCount = rsmeta.getColumnCount();
        HashMap<String, String> coltypes = new HashMap<>();
        HashMap<String, Integer> displaysize = new HashMap<>();
        HashMap<String, Integer> numbers = new HashMap<>();
        HashMap<String, Integer> decimals = new HashMap<>();
        for (int i = 1; i <= colCount; i++) {
            colNames.add(rsmeta.getColumnLabel(i));
            coltypes.put(rsmeta.getColumnLabel(i), rsmeta.getColumnTypeName(i));
            displaysize.put(rsmeta.getColumnLabel(i), rsmeta.getColumnDisplaySize(i));
            numbers.put(rsmeta.getColumnLabel(i), rsmeta.getPrecision(i));
            decimals.put(rsmeta.getColumnLabel(i), rsmeta.getScale(i));
            LOG.trace("Table: " + tableName
                    + " - column: " + rsmeta.getColumnLabel(i) // Name der Column
                    + " - Type: " + rsmeta.getColumnTypeName(i) // Typ: z.B. CHAR
                    + " - Display Size: " + rsmeta.getColumnDisplaySize(i) // Ges. Stellen; bei NUMERIC Vor- + Nachkommastellen
                    + " - Precision: " + rsmeta.getPrecision(i) // Vorkommastellen
                    + " - Scale: " + rsmeta.getScale(i));       // Nachkommastellen
        }
        LOG.debug("Reading meta data from table " + tableName + " complete");

        return new MetaInfoTable(tableName, colNames, coltypes, displaysize, numbers, decimals);
    }

    /**
     * Check if a table exists in the database.
     * <p>
     * @param con the JDBC connection to the database
     * @param tbl the table name
     *
     * @return true if the table exists, otherwise false
     */
    public boolean testTableExists(Connection con, String tbl) {
        LOG.debug("Test if table " + tbl + " is present...");
        try {
            Statement stmt = con.createStatement();
            ResultSet rset = stmt.executeQuery("Select 'Hello' from " + tbl);
            if (rset.getRow() != 0) {
                return (false);
            }
        }
        catch (SQLException sqlex) {
            LOG.debug("Table " + tbl + " NOT found...");
            return (false);
        }
        LOG.debug("Table " + tbl + " found...");

        return (true);
    }

    /**
     * Rename the 'shadow' table to the 'target' name.
     *
     * @param con    the database connection
     * @param shadow the 'shadow' table name
     * @param target the 'target' table name
     *
     * @throws SQLException on any SQL exception
     */
    public void renameShadowToTarget(Connection con, String shadow, String target) throws SQLException {
        Statement stmt;
        stmt = con.createStatement();
        if (testTableExists(con, target)) {
            stmt.executeUpdate("Drop table " + target);
            LOG.debug("Table " + target + " exists. Table dropped.");
        }
        stmt.executeUpdate("Alter table " + shadow + " rename to " + target);
        LOG.info("Table " + shadow + " renamed to " + target);
    }

    /**
     * Cleanup the JDBC connection by shutting down the connection pool.
     *
     * @throws java.sql.SQLException in case of any SQL exception
     */
    public void cleanupJdbc() throws SQLException {
        LOG.info("Cleanup JDBC connection for " + dtp.getConnectString() + " for user " + dtp.getUserName());

        if (connectionPool != null) {
            connectionPool.shutdown();
            LOG.debug("Connection Pool closed...");
            connectionPool = null;
        }
    }
}
