package dev.multidownloads.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This method reads the configurations for the basic download parameters
 * 
 * @author vanvu
 *
 */
public class Config {
	final static Logger logger = LogManager.getLogger(Config.class);
	private static Properties defaultProps = new Properties();
	
	/**
	 * Load the default configuration file
	 */
	static {
		try {
			FileInputStream in = new FileInputStream("./config.properties");
			defaultProps.load(in);
			in.close();
		} catch (Exception e) {
			logger.warn("Found no config file. To use hard-coded download parameters");
		}
	}
	
	/**
	 * Map of default parameters
	 * To be used when no default configuration file found or the configured value is wrong
	 */
	private static final Map<String, String> defaultParametersMap;
    static
    {
        defaultParametersMap = new HashMap<String, String>();
        defaultParametersMap.put("NUM_OF_PARALLEL_DOWNLOAD", "2");
        defaultParametersMap.put("MAX_NUM_OF_RETRY", "2");
        defaultParametersMap.put("DELAY_IN_SECONDS", "60");
        defaultParametersMap.put("NUM_OF_CONCURRENT_CONNECTION", "5");
        defaultParametersMap.put("TIMEOUT_IN_SECONDS", "30");
        defaultParametersMap.put("SEGMENTATION_SIZE_IN_KB", "1024");
        defaultParametersMap.put("SEPARATOR", ";");
        defaultParametersMap.put("BUFFER_SIZE_IN_KB", "128");
        defaultParametersMap.put("TIMEOUT_IN_SECONDS", "30");
        defaultParametersMap.put("NETWORK_TIMEOUT_IN_MILLISECONDS", "10000");
        defaultParametersMap.put("DEFAULT_FTP_CREDENTIAL", "anonymous");
        
        StringBuilder sb = new StringBuilder(System.getProperty("user.home"))
        						.append(File.separator).append("DL")
        						.append(File.separator);
		defaultParametersMap.put("DOWNLOAD_DIR", sb.toString());
    }
	
    /**
     * Return as parameter as string
     * @param key Name of the parameter
     * @return Its value
     */
	public static String getParameterAsString(String key) {
		return getProperty(key) == null ? defaultParametersMap.get(key): getProperty(key);
	}
	
	/**
     * Return as parameter as an integer
     * @param key Name of the parameter
     * @return Its value
     */
	public static int getParameterAsInteger(String key) {
		int parameter = 0;
		try {
			parameter = Integer.valueOf(getProperty(key));
		} catch(NumberFormatException e) {
			try {
				parameter = Integer.valueOf(defaultParametersMap.get(key));
			} catch(NumberFormatException nfe) {
				logger.error("Something extremely wrong!!!");
			}
			logger.warn("No configuration of {} found. To use the default value {}", key, parameter);
		}
		return parameter;
	}

	/**
	 * This method returns the value of a configuration parameter
	 * 
	 * @param key Key of a parameter
	 * @return Its value
	 */
	private static String getProperty(String key) {
		return defaultProps.getProperty(key);
	}
}
