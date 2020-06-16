package com.home.dbimportermaven.misc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handle DB parameters.
 */
public abstract class Parameters {
    private static final Logger LOG = LogManager.getLogger(Parameters.class.getName());

    private static final String DEFAULTS = "DEFAULTS";
    private final String propertiesFilename;
    private final String propertiesDescription;
    private String propertiesOrigin;
    /**
     * The properties used as parameters.
     */
    protected Properties properties = null;

    /**
     * Buildup the Parameters.
     *
     * @param propertiesFilename    the properties file to use
     * @param propertiesDescription the properties description
     */
    protected Parameters(String propertiesFilename, String propertiesDescription) {
        this.propertiesFilename = propertiesFilename;
        this.propertiesDescription = propertiesDescription;
        this.propertiesOrigin = DEFAULTS;
    }

    /**
     * Set the database defaults.
     *
     * @param defaults the default propeties
     */
    abstract public void setDefaults(Properties defaults);

    /**
     * Update the properties from settings.
     */
    abstract public void updatePropertiesFromSettings();

    /**
     * Update the settings form properties.
     */
    abstract public void updateSettingsFromProperties();

    /**
     * Get the properties origin.
     *
     * @return the origin
     */
    protected String getPropertiesOrigin() {
        return propertiesOrigin;
    }

    /**
     * Get parametes from a property file.
     * <p>
     * In case the properties file is not accessable default values are set.
     */
    public void getParameters() {
        Properties defaults = new Properties();
        FileInputStream in = null;
        String folder = getConfigFolder();
        String filesep = System.getProperty("file.separator");

        setDefaults(defaults);

        properties = new Properties(defaults);

        try {
            in = new FileInputStream(folder
                    + filesep
                    + propertiesFilename);
            properties.load(in);
            this.propertiesOrigin = folder + filesep + propertiesFilename;
        }
        catch (java.io.FileNotFoundException e) {
            LOG.error("Can't find properties file " + folder + filesep + propertiesFilename + ". Using defaults.");
        }
        catch (java.io.IOException e) {
            LOG.error("Can't read properties file " + folder + filesep + propertiesFilename + ". Using defaults.");
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (java.io.IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }

        updateSettingsFromProperties();
    }

    /**
     * Save the parameters in a property file.
     */
    public void saveParameters() {
        updatePropertiesFromSettings();

        LOG.debug("Just set properties: " + propertiesDescription);
        LOG.debug(toString());

        FileOutputStream out = null;

        try {
            String folder = getConfigFolder();
            String filesep = System.getProperty("file.separator");
            out = new FileOutputStream(folder
                    + filesep
                    + propertiesFilename);
            properties.store(out, propertiesDescription);
        }
        catch (java.io.IOException e) {
            LOG.error("Can't save properties. "
                    + "Oh well, it's not a big deal.");
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (java.io.IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Get the name of the folder where the configuration file is placed.
     * <p>
     * Can be defines as a system property named "ConfigFolder". If the system property does not exist or is empty than
     * the value of system property "user.dir" is used
     *
     * @return the configuration folder name
     */
    public String getConfigFolder() {
        String folder = System.getProperty("ConfigFolder");

        if (folder == null) {
            folder = System.getProperty("user.dir");
        }
        else {
            String tmp = folder.trim();

            if (tmp.equals("")) {
                folder = System.getProperty("user.dir");
            }
            else {
                LOG.debug("Folder is " + folder);
            }
        }

        return folder;
    }
}
