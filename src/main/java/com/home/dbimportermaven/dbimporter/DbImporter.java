package com.home.dbimportermaven.dbimporter;

import com.home.dbimportermaven.jobs.CopyJob;
import com.home.dbimportermaven.misc.DbHandler;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The DbImport starter.
 */
public class DbImporter implements DbImporterMBean {
    private static final Logger LOG = LogManager.getLogger(DbImporter.class.getName());

    private static final long DBIMPORTER_TIMEOUT_DEFAULT = 10000L;
    private static final long SHUTDOWN_TIMEOUT = 2000L;
    private long dbImporterTimeout;
    private boolean stop = false;

    private static DbHandler sourceDbHandler = null;
    private static DbHandler targetDbHandler = null;
    private static PropertiesReader propReader = null;
    private static String[] sourceTableNames;
    private static ScheduledExecutorService scheduledExecutorService = null;
    private static final HashMap<String, ScheduledFuture> jobs = new HashMap<>();

    /**
     * The start method to execute.
     *
     * @throws ClassNotFoundException in case the class is not found
     * @throws SQLException           in case of any SQL exception
     */
    public void start() throws ClassNotFoundException, SQLException {
        LOG.info("DBImporter started");
        try {
            sourceDbHandler = new DbHandler(new DbParameters("SourceDb.properties", "Source Database Parameters"));
            targetDbHandler = new DbHandler(new DbParameters("TargetDb.properties", "Target Database Parameters"));
            propReader = new PropertiesReader("config.properties", sourceDbHandler);
            sourceTableNames = propReader.getSourceTableNames();
            // The number of source tables defines how many threads have to be scheduled
            scheduledExecutorService = Executors.newScheduledThreadPool(propReader.getSourceTableNames().length);

            // check if selected source tables exist
            if (!propReader.checkSourceTables(sourceDbHandler.getConnection())) {
                // Error case; Exit program
                throw new IllegalStateException("Source table check failed");
            }
            LinkedList<String> DDL_Strings = propReader.generateDDL(sourceDbHandler.getConnection());
            LinkedList<String> newTableNames = propReader.getTargetTableNames();
            targetDbHandler.generateTables(targetDbHandler.getConnection(), DDL_Strings, newTableNames);

            for (String sourceTableName : sourceTableNames) {
                ScheduledFuture thread = scheduledExecutorService.scheduleAtFixedRate(
                        new CopyJob(sourceDbHandler, targetDbHandler, propReader, sourceTableName), 1, propReader.gettimeout(sourceTableName), TimeUnit.SECONDS);
                jobs.put(sourceTableName, thread);
            }

            for (;;) {
                while (!stop) {
                    dbImporterTimeout = getDbImporterTimeout();
                    Thread.sleep(dbImporterTimeout);

                    sourceDbHandler.showConnectionStatus();
                    targetDbHandler.showConnectionStatus();
                }
                Thread.sleep(dbImporterTimeout);

                sourceDbHandler.showConnectionStatus();
                targetDbHandler.showConnectionStatus();
            }
        }
        catch (SQLException sqlex) {
            while (sqlex != null) {
                LOG.fatal("SQL-Exception: " + sqlex.getMessage());
                sqlex = sqlex.getNextException();
            }
        }
        catch (Exception e) {
            LOG.fatal("NON-SQL-Exception: " + e.getMessage());
        }
        finally {
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
            }

            if (sourceDbHandler != null) {
                sourceDbHandler.cleanupJdbc();
            }

            if (targetDbHandler != null) {
                targetDbHandler.cleanupJdbc();
            }
        }
    }

    public DbImporter() {
        this.dbImporterTimeout = DBIMPORTER_TIMEOUT_DEFAULT;
    }

    @Override
    public void setDbImporterTimeout(long timeout) {
        this.dbImporterTimeout = timeout;
    }

    @Override
    public long getDbImporterTimeout() {
        return this.dbImporterTimeout;
    }

    @Override
    public void stop() {
        if (stop == false) {
            stop = true;
            for (String sourceTableName : sourceTableNames) {
                jobs.get(sourceTableName).cancel(false);
            }
            LOG.info("Stopped...");
        }
        else {
            LOG.warn("Already stopped...");
        }
    }

    @Override
    public void restart() throws SQLException {
        if (stop == true) {
            stop = false;
            for (String sourceTableName : sourceTableNames) {
                ScheduledFuture thread = scheduledExecutorService.scheduleAtFixedRate(
                        new CopyJob(sourceDbHandler, targetDbHandler, propReader, sourceTableName), 1, propReader.gettimeout(sourceTableName), TimeUnit.SECONDS);
                jobs.put(sourceTableName, thread);
            }
            LOG.info("Restarted...");
        }
        else {
            LOG.warn("Already started...");
        }
    }

    @Override
    public void shutdown() throws InterruptedException {
        stop();
        Thread.sleep(SHUTDOWN_TIMEOUT);
        LOG.info("Shutdown " + DbImporter.class.getSimpleName() + " now...");
        System.exit(0);
    }
}
