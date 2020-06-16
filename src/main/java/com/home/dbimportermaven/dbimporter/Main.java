package com.home.dbimportermaven.dbimporter;

import java.lang.management.*;
import javax.management.*;

/**
 * The Main class for starting DbImporter.
 */
public class Main {
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
        ObjectName name = new ObjectName("com.home.dbimportermaven.dbimporter:type=DbImporter");
        DbImporter mbean = new DbImporter();
        mbs.registerMBean(mbean, name);

        // The start is looping forever what is needed here otherwise the following is needed:
        // Thread.sleep(Long.MAX_VALUE);
        mbean.start();
    }
}
