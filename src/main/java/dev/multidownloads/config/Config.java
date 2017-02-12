package dev.multidownloads.config;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static Properties defaultProps = new Properties();
	
	static {
		try {
			FileInputStream in = new FileInputStream("./config.properties");
	        defaultProps.load(in);
	        in.close();
	    } catch (Exception e) {
	    	logger.log(Level.SEVERE, "Found no config file. To use hard-coded download parameters");
	    }
	}
	
	public static String getProperty(String key) {
		return defaultProps.getProperty(key);
	}
}
