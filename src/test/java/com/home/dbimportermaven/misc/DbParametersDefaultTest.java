package com.home.dbimportermaven.misc;

import java.util.Properties;
import junit.framework.TestCase;

/**
 * Test DbPaameterDefaults
 */
public class DbParametersDefaultTest extends TestCase {

    public DbParametersDefaultTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of testSetDefaults method, of class DbParametersDefault.
     */
    public void testSetDefaults() {
        System.out.println("setDefaults");
        Properties props = new Properties();
        DbParametersDefault instance = new DbParametersDefault("", "Test");
        instance.setDefaults(props);

        assertFalse(props.isEmpty());

        assertTrue(props.containsKey(DbParametersIF.CONNECT_STRING_KEY));
        assertTrue(props.containsKey(DbParametersIF.DRIVER_KEY));
        assertTrue(props.containsKey(DbParametersIF.PASSWORD_KEY));
        assertTrue(props.containsKey(DbParametersIF.TABLE_NAME_KEY));
        assertTrue(props.containsKey(DbParametersIF.TRANS_ISOLA_KEY));
        assertTrue(props.containsKey(DbParametersIF.USER_NAME_KEY));

        assertTrue(props.containsValue(DbParametersIF.CONNECT_STRING_DEFAULT));
        assertTrue(props.containsValue(DbParametersIF.DRIVER_DEFAULT));
        assertTrue(props.containsValue(DbParametersIF.PASSWORD_DEFAULT));
        assertTrue(props.containsValue(DbParametersIF.TABLENAME_DEFAULT));
        assertTrue(props.containsValue(Integer.toString(DbParametersIF.TRANS_ISOLA_DEFAULT)));
        assertTrue(props.containsValue(DbParametersIF.USERNAME_DEFAULT));
    }
}
