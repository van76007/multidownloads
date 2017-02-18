package dev.multidownloads.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
	final static Logger logger = LogManager.getLogger(Config.class);
	private static Properties defaultProps = new Properties();
	
	static {
		try {
			FileInputStream in = new FileInputStream("./config.properties");
	        defaultProps.load(in);
	        in.close();
	    } catch (Exception e) {
	    	logger.error("Found no config file. To use hard-coded download parameters", e);
	    }
	}
	
	public static String getProperty(String key) {
		return defaultProps.getProperty(key);
	}
}
