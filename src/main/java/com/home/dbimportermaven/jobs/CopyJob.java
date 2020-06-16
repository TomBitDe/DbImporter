package com.home.dbimportermaven.jobs;

import com.home.dbimportermaven.dbimporter.PropertiesReader;
import com.home.dbimportermaven.misc.DbHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author z001xkzs
 */
public class CopyJob implements Runnable {
    private static final Logger LOG = LogManager.getLogger(CopyJob.class.getName());
    private final DbHandler sourceDBHandler;
    private final DbHandler targetDBHandler;
    private final PropertiesReader propReader;
    private final String srcTableName;
    private LinkedList<LinkedList<String>> data;
    private String selectStr;
    private final String targetTableName;
    private Connection srcCon;
    private Connection targetCon;

    /**
     * Establish the copy job.
     *
     * @param sourceDBHandler the 'source' database handler
     * @param targetDBHandler the 'target' database handler
     * @param propReader      the properties reader to use
     * @param srcTableName    the table to operate on
     *
     * @throws SQLException on any SQL exception
     */
    public CopyJob(DbHandler sourceDBHandler, DbHandler targetDBHandler, PropertiesReader propReader, String srcTableName) throws SQLException {
        this.sourceDBHandler = sourceDBHandler;
        this.targetDBHandler = targetDBHandler;
        this.propReader = propReader;
        this.srcTableName = srcTableName;
        this.srcCon = null;
        this.targetCon = null;
        selectStr = null;
        targetTableName = this.propReader.getTargetName(this.srcTableName);
        LOG.info("Thread for table " + targetTableName + " created");
    }

    @Override
    public void run() {

        try {
            srcCon = sourceDBHandler.getConnection();
            targetCon = targetDBHandler.getConnection();

            if (selectStr == null) {
                selectStr = propReader.generateSelectString(srcCon, srcTableName);
            }

            String shadowTableName = targetTableName + "_SHADOW";
            LOG.info("Creating new Shadow Table " + shadowTableName);
            targetDBHandler.generateSingleTable(targetCon, propReader.createShadowTableString(srcTableName, targetTableName, shadowTableName), shadowTableName);

            LOG.info("Starting Data Refresh on Table " + shadowTableName);
            data = sourceDBHandler.getData(srcCon, selectStr, srcTableName);

            LOG.info("Data gathering completed for table " + targetTableName);
            targetDBHandler.insertData(targetCon, data, shadowTableName);

            LOG.info("Data Refresh on Table " + shadowTableName + " completed");

            targetDBHandler.renameShadowToTarget(targetCon, shadowTableName, targetTableName);
        }
        catch (SQLException ex) {
            LOG.fatal("Error in Thread for table " + targetTableName + ". " + ex.getMessage());
        }
        finally {
            try {
                srcCon.close();
                targetCon.close();
                LOG.debug("Connection closed in thread " + targetTableName);
            }
            catch (SQLException ex) {
                LOG.error("Cannot close connection in thread " + targetTableName);
            }
        }
    }
}
