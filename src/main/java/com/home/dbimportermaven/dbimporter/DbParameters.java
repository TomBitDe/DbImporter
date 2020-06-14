package com.home.dbimportermaven.dbimporter;

import com.home.dbimportermaven.misc.DbParametersDefault;
import com.home.dbimportermaven.misc.DbParametersIF;

/**
 *
 */
public class DbParameters implements DbParametersIF {
    private final DbParametersDefault dbParameters;

    /**
     * Creates new SourceDbParameters
     */
    public DbParameters() {
        dbParameters = new DbParametersDefault("db.properties", "Database Parameters");
        dbParameters.getParameters();
    }

    public DbParameters(String fileName, String description) {
        dbParameters = new DbParametersDefault(fileName, description);
        dbParameters.getParameters();
    }

    @Override
    public void setDriver(String driver) {
        dbParameters.setDriver(driver);
        dbParameters.saveParameters();
    }

    @Override
    public String getDriver() {
        return dbParameters.getDriver();
    }

    @Override
    public void setConnectString(String connectString) {
        dbParameters.setConnectString(connectString);
        dbParameters.saveParameters();
    }

    @Override
    public String getConnectString() {
        return dbParameters.getConnectString();
    }

    @Override
    public void setUserName(String userName) {
        dbParameters.setUserName(userName);
        dbParameters.saveParameters();
    }

    @Override
    public String getUserName() {
        return dbParameters.getUserName();
    }

    @Override
    public void setPassword(String password) {
        dbParameters.setPassword(password);
        dbParameters.saveParameters();
    }

    @Override
    public String getPassword() {
        return dbParameters.getPassword();
    }

    @Override
    public void setTableName(String tableName) {
        dbParameters.setTableName(tableName);
        dbParameters.saveParameters();
    }

    @Override
    public String getTableName() {
        return dbParameters.getTableName();
    }

    @Override
    public void setTransactionIsolation(int transIsola) {
        dbParameters.setTransactionIsolation(transIsola);
        dbParameters.saveParameters();
    }

    @Override
    public int getTransactionIsolation() {
        return dbParameters.getTransactionIsolation();
    }
}
