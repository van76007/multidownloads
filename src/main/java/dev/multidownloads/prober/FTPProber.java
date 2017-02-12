package dev.multidownloads.prober;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;

public class FTPProber implements Prober {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 10000;

	@Override
	public void probeResource(DownloadInfor infor) {
		try {
			URLConnection conn = new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of FTP connection TIMEOUT");
			}
			conn.setConnectTimeout(timeout);
			int len = conn.getContentLength();
			
			infor.setFileLength(len);
			infor.setValid(len != -1 ? true : false);	
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder("Error in detecting file length of resource: ").append(infor.getUrl());
			logger.log(Level.WARNING, sb.toString(), e);
		}
		
	}

}
