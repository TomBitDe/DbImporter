package com.home.dbimportermaven.dbimporter;

import java.sql.SQLException;

/**
 */
public interface DbImporterMBean {
    public void setDbImporterTimeout(long timeout);

    public long getDbImporterTimeout();

    public void stop();

    public void restart() throws SQLException;

    public void shutdown() throws InterruptedException;
}
