package com.home.dbimportermaven.misc;

/**
 * Access key constants and access methods for DB parameters.
 */
public interface DbParametersIF {
    // These are the VALID KEYS for DbParameters class
    public static final String DRIVER_KEY = "jdbc.driver";
    public static final String CONNECT_STRING_KEY = "jdbc.connectstring";
    public static final String USER_NAME_KEY = "jdbc.username";
    public static final String PASSWORD_KEY = "jdbc.password";
    public static final String TABLE_NAME_KEY = "test.tablename";
    public static final String TRANS_ISOLA_KEY = "test.transactionisolation";

    // These are DEFAULT values for the specified KEYS
    public static final String DRIVER_DEFAULT = "oracle.jdbc.driver.OracleDriver";
    public static final String CONNECT_STRING_DEFAULT = "jdbc\\:oracle\\:thin\\:@hostname\\:Port\\:SID";
    public static final String USERNAME_DEFAULT = System.getProperty("user.name");
    public static final String PASSWORD_DEFAULT = "<Enter password for " + USERNAME_DEFAULT + " here>";
    public static final String TABLENAME_DEFAULT = "testdata";
    public static final int TRANS_ISOLA_DEFAULT = 4;

    public void setDriver(String driver);

    public String getDriver();

    public void setConnectString(String connectString);

    public String getConnectString();

    public void setUserName(String userName);

    public String getUserName();

    public void setPassword(String password);

    public String getPassword();

    public void setTableName(String tableName);

    public String getTableName();

    public void setTransactionIsolation(int transIsola);

    public int getTransactionIsolation();
}
