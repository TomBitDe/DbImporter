package com.home.dbimportermaven.dbimporter;

import com.home.dbimportermaven.dbtypes.MetaInfoTable;
import com.home.dbimportermaven.misc.DbHandler;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class PropertiesReader {
    private static final Logger LOG = LogManager.getLogger(PropertiesReader.class.getName());

    private Properties prop;
    private final DbHandler dbHandler;
    private String[] orgTable;
    private final HashMap<String, String> createStrings;

    public PropertiesReader(String fileName, DbHandler dbHandler) {
        this.dbHandler = dbHandler;
        createStrings = new HashMap<>();
        orgTable = new String[100];
        InputStream in = null;

        try {
            prop = new Properties();
            in = new FileInputStream(fileName);
            prop.load(in);
            orgTable = prop.getProperty("sourcetables").split(" ");
        }
        catch (java.io.FileNotFoundException e) {
            LOG.error("Can't find properties file: " + fileName);
        }
        catch (java.io.IOException e) {
            LOG.error("Can't read properties file " + fileName);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (java.io.IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    public boolean checkSourceTables(Connection con) {
        LOG.info("checking SourceTables");
        for (String orgTable1 : orgTable) {
            if (!dbHandler.testTableExists(con, orgTable1)) {
                LOG.fatal("Configuration-Error: Cannot find table " + orgTable1 + " in source Database");
                LOG.fatal("Program is stopping due Error");
                throw new IllegalStateException("Configuration-Error: Cannot find table " + orgTable1 + " in source Database");
            }
        }
        return true;
    }

    public String[] getSourceTableNames() {
        return prop.getProperty("sourcetables").split(" ");
    }

    public int gettimeout(String srcTableName) {
        return Integer.parseInt(prop.getProperty(srcTableName + ".timeout"));
    }

    public LinkedList<String> getTargetTableNames() {
        LinkedList<String> result = new LinkedList<>();

        for (String orgTable1 : orgTable) {
            String target = prop.getProperty(orgTable1 + ".target");
            if (target.equals("")) {
                target = orgTable1;
            }
            result.add(target);
        }
        return result;
    }

    public String getTargetName(String srcTableName) {
        String target = prop.getProperty(srcTableName + ".target");
        if (target.equals("")) {
            target = srcTableName;
        }
        return target;
    }

    public LinkedList<String> generateDDL(Connection con) throws SQLException {
        LinkedList<String> result = new LinkedList<>();
        for (String orgTable1 : orgTable) {
            MetaInfoTable tableMeta = dbHandler.getMetaTableInfo(con, orgTable1);
            String target = prop.getProperty(orgTable1 + ".target");
            if (target.equals("")) {
                target = orgTable1;
            }
            String[] columns = prop.getProperty(orgTable1 + ".columns").split(" ");
            if (columns.length <= 1) {
                columns = new String[tableMeta.getColNames().size()];
                tableMeta.getColNames().toArray(columns);
            }
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("Create table ").append(target).append(" (");
            for (int col = 0; col < columns.length; col++) {
                sBuilder.append(columns[col]).append(" ");
                LOG.debug("Index=" + col + ": Column=[" + columns[col] + "] Type=[" + tableMeta.getColtype(columns[col]) + "]");
                if (tableMeta.getColtype(columns[col]).equals("NUMBER")) {
                    sBuilder.append("DECIMAL (").append(tableMeta.getNumbers(columns[col])).append(",")
                            .append(tableMeta.getDecimals(columns[col])).append(") NOT NULL");
                }
                if (tableMeta.getColtype(columns[col]).equals("TIMESTAMP")) {
                    sBuilder.append("TIMESTAMP NOT NULL");
                }
                if (tableMeta.getColtype(columns[col]).equals("INTEGER")) {
                    sBuilder.append("INTEGER NOT NULL");
                }
                if ((tableMeta.getColtype(columns[col]).equals("CHAR"))
                        || (tableMeta.getColtype(columns[col]).equals("VARCHAR"))
                        || (tableMeta.getColtype(columns[col]).equals("VARCHAR2"))) {
                    sBuilder.append("CHAR (").append(tableMeta.getDisplaySize(columns[col])).append(") NOT NULL");
                }
                if (tableMeta.getColtype(columns[col]).equals("BLOB")) {
                    throw new SQLException("Table " + target + "; this tool does not support BLOB type");
                }
                if (col < columns.length - 1) {
                    sBuilder.append(", ");
                }
            }
            sBuilder.append(")");
            LOG.debug(sBuilder.toString());
            createStrings.put(orgTable1, sBuilder.toString());
            result.add(sBuilder.toString());
        }
        return result;
    }

    public String createShadowTableString(String srcTable, String orgTable, String sdwTable) {
        String sqlStr = createStrings.get(srcTable);
        if (sqlStr != null) {
            sqlStr = sqlStr.replace(" " + orgTable + " ", " " + sdwTable);
            LOG.debug("Shadow table create String: " + sqlStr);
            return sqlStr;
        }
        else {
            LOG.fatal("Cannot create Shadow Table for table [" + orgTable + "]");
            throw new IllegalStateException("Cannot create Shadow Table for table [" + orgTable + "]");
        }
    }

    public String generateSelectString(Connection con, String srcTableName) throws SQLException {
        String columns[] = prop.getProperty(srcTableName + ".columns").split(" ");
        if ((columns.length <= 0) || (columns[0].trim().equals(""))) {
            MetaInfoTable tableMeta = dbHandler.getMetaTableInfo(con, srcTableName);
            columns = new String[tableMeta.getColNames().size()];
            tableMeta.getColNames().toArray(columns);
        }
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("Select ");

        for (int i = 0; i < columns.length; i++) {
            sBuilder.append(columns[i]);
            if (i < columns.length - 1) {
                sBuilder.append(", ");
            }
            else {
                sBuilder.append(" from ").append(srcTableName);
            }
        }

        String selcond = prop.getProperty(srcTableName + ".where", "");
        if (!selcond.trim().isEmpty()) {
            sBuilder.append(" where ").append(selcond);
        }

        LOG.debug(sBuilder.toString());
        return sBuilder.toString();
    }
}
