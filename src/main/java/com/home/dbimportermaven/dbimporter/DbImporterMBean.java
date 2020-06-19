package com.home.dbimportermaven.dbimporter;

import java.sql.SQLException;

/**
 * The MBean interface of DbImporter.
 */
public interface DbImporterMBean {
    /**
     * Set the import executor timeout
     *
     * @param timeout the timeout value in millisec
     */
    public void setDbImporterTimeout(long timeout);

    /**
     * Get the import executor timeout
     *
     * @return the timeout value in millisec
     */
    public long getDbImporterTimeout();

    /**
     * Stop the importer task
     *
     * @throws InterruptedException in case of any interruption
     */
    public void stop() throws InterruptedException;

    /**
     * Restart the importer task
     *
     * @throws SQLException in case of any SQL exception
     */
    public void restart() throws SQLException;

    /**
     * Shutdown the application
     *
     * @throws InterruptedException in case of any interruption
     */
    public void shutdown() throws InterruptedException;
}
