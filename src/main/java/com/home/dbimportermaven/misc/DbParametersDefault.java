package com.home.dbimportermaven.misc;

import java.util.Properties;

/**
 * The DB parameter default values.
 */
public class DbParametersDefault extends Parameters implements DbParametersIF {
    private String driver;
    private String connectString;
    private String userName;
    private String password;
    private String tableName;
    private int transIsola;

    public DbParametersDefault(String propertiesFilename, String propertiesDescription) {
        super(propertiesFilename, propertiesDescription);
    }

    @Override
    public void updateSettingsFromProperties() {
        driver = properties.getProperty(DRIVER_KEY);
        connectString = properties.getProperty(CONNECT_STRING_KEY);
        userName = properties.getProperty(USER_NAME_KEY);
        password = properties.getProperty(PASSWORD_KEY);
        tableName = properties.getProperty(TABLE_NAME_KEY);

        String tmp = properties.getProperty(TRANS_ISOLA_KEY);
        transIsola = Integer.parseInt(tmp);
    }

    @Override
    public void setDefaults(Properties defaults) {
        defaults.put(DRIVER_KEY, DRIVER_DEFAULT);
        defaults.put(CONNECT_STRING_KEY, CONNECT_STRING_DEFAULT);
        defaults.put(USER_NAME_KEY, USERNAME_DEFAULT);
        defaults.put(PASSWORD_KEY, PASSWORD_DEFAULT);
        defaults.put(TABLE_NAME_KEY, TABLENAME_DEFAULT);
        defaults.put(TRANS_ISOLA_KEY, Integer.toString(TRANS_ISOLA_DEFAULT));
    }

    @Override
    public void updatePropertiesFromSettings() {
        properties.put(DRIVER_KEY, driver);
        properties.put(CONNECT_STRING_KEY, connectString);
        properties.put(USER_NAME_KEY, userName);
        properties.put(PASSWORD_KEY, password);
        properties.put(TABLE_NAME_KEY, tableName);
        properties.put(TRANS_ISOLA_KEY, Integer.toString(transIsola));
    }

    @Override
    public String toString() {
        return "["
                + driver + ","
                + connectString + ","
                + userName + ","
                + password + ","
                + tableName + ","
                + transIsola + "]";
    }

    @Override
    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    @Override
    public String getConnectString() {
        return connectString;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTransactionIsolation(int transIsola) {
        this.transIsola = transIsola;
    }

    @Override
    public int getTransactionIsolation() {
        return transIsola;
    }
}
