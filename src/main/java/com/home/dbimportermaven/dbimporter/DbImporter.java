package com.home.dbimportermaven.dbimporter;

import com.home.dbimportermaven.jobs.CopyJob;
import com.home.dbimportermaven.misc.DbHandler;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.io.monitor.FileEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The DbImport starter.
 */
public class DbImporter implements DbImporterMBean {
    private static final Logger LOG = LogManager.getLogger(DbImporter.class.getName());

    private static final String DBIMPORTER_CONFIG = "config.properties";
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
    private static final File MONITORED_CONFIG_FILE = new File(DBIMPORTER_CONFIG);
    private static final FileEntry monitoredFileEntry = new FileEntry(MONITORED_CONFIG_FILE);

    /**
     * The start method to execute
     *
     * @throws ClassNotFoundException in case the class is not found
     * @throws SQLException           in case of any SQL exception
     */
    @SuppressWarnings("SleepWhileInLoop")
    public void start() throws ClassNotFoundException, SQLException {
        LOG.info("DBImporter started");
        try {
            sourceDbHandler = new DbHandler(new DbParameters("SourceDb.properties", "Source Database Parameters"));
            targetDbHandler = new DbHandler(new DbParameters("TargetDb.properties", "Target Database Parameters"));

            setupJobs();

            monitoredFileEntry.refresh(MONITORED_CONFIG_FILE);

            for (;;) {
                while (!stop) {
                    if (monitoredFileEntry.refresh(MONITORED_CONFIG_FILE)) {
                        stop();
                        restart();
                    }

                    Thread.sleep(getDbImporterTimeout());

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

    /**
     * Create a new DbImporter
     */
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
    public void stop() throws InterruptedException {
        if (stop == false) {
            stop = true;
            cancelJobs();
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
            setupJobs();
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

    /**
     * Do every thing that is needed to setup the DbImporter jobs
     *
     * @throws SQLException in case of any SQL exception
     */
    private void setupJobs() throws SQLException {
        propReader = new PropertiesReader(DBIMPORTER_CONFIG, sourceDbHandler);
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
    }

    /**
     * Cancel the DbImporter jobs that are currently running or scheduled
     *
     * @throws InterruptedException in case of any interruption
     */
    private void cancelJobs() throws InterruptedException {
        for (String sourceTableName : sourceTableNames) {
            jobs.get(sourceTableName).cancel(false);
            LOG.info("Canceled job for " + sourceTableName);
        }
        LOG.info("Shutdown ScheduledExecutorService...");
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        LOG.info("ScheduledExecutorService terminated...");
    }

    /**
     * The starter for registering as MBean and starting DbImporter.<br>
     * Currently no arguments needed
     *
     * @param args the starters arguments
     *
     * @throws Exception in case of any exception
     */
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        DbImporter mbean = new DbImporter();
        String mbo = mbean.getClass().getPackage().getName()
                + ":type="
                + mbean.getClass().getSimpleName();
        ObjectName name = new ObjectName(mbo);
        mbs.registerMBean(mbean, name);

        // The start is looping forever what is needed here otherwise the following is needed:
        // Thread.sleep(Long.MAX_VALUE);
        mbean.start();
    }
}
