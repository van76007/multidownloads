package dev.multidownloads.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This method reads the configurations for the basic download parameters
 * @author vanvu
 *
 */
public class Config {
	final static Logger logger = LogManager.getLogger(Config.class);
	private static Properties defaultProps = new Properties();
	
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
	 * This method returns the value of a configuration parameter
	 * @param key Key of a parameter
	 * @return Its value
	 */
	public static String getProperty(String key) {
		return defaultProps.getProperty(key);
	}
}
