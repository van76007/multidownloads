package dev.multidownloads;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DownloadLogger {
	public static void setup() throws IOException {
		Logger logger = Logger.getLogger("dev.multidownloads");
		addLoggingHandlers(logger);
		setLoggingLevels(logger);
	}

	private static void addLoggingHandlers(Logger logger) throws IOException {
		logger.addHandler(new ConsoleHandler());
		String date = new SimpleDateFormat("yyy-MM-dd_HH_mm").format(new Date(System.currentTimeMillis()));
		Handler handler = new FileHandler("./download_" + date + "_%u.log", 1024*1024, 50, true);
		logger.addHandler(handler);
		handler.setFormatter(new SimpleFormatter());
	}
	
	private static void setLoggingLevels(Logger logger) {
		Handler[] handlers = logger.getHandlers();
		for(int index = 0; index < handlers.length; index++ ) {
			handlers[index].setLevel(Level.FINE);
		}
		logger.setLevel(Level.FINE);
	}
}
