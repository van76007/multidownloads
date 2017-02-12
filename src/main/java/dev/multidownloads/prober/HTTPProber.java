package dev.multidownloads.prober;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;

public class HTTPProber implements Prober {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 10000;

	@Override
	public void probeResource(DownloadInfor infor) {
		inquiryIfSupportRangeHeader(infor);
		inquiryFileLength(infor);
	}

	private void inquiryIfSupportRangeHeader(DownloadInfor infor) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of HTTP connection TIMEOUT");
			}
			conn.setConnectTimeout(timeout);
			conn.setRequestProperty("Range", "bytes=0-1");
			conn.connect();
			
			int len = conn.getContentLength();
			infor.setFileLength(len);
			infor.setValid(len != -1 ? true : false);
			String range = conn.getHeaderField("Content-Range");
			infor.setSupportMultiPartsDownload(range != null ? true : false);
			
			conn.disconnect();
		} catch (IOException e) {
			infor.setValid(false);
			StringBuilder sb = new StringBuilder("Error in detecting if server support multi-part download of of resource: ").append(infor.getUrl());
			logger.log(Level.SEVERE, sb.toString(), e);
		}
	}
	
	private void inquiryFileLength(DownloadInfor infor) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of HTTP connection TIMEOUT");
			}
			conn.setConnectTimeout(timeout);
			conn.connect();
			
			int len = conn.getContentLength();
			infor.setFileLength(len);
			infor.setValid(len != -1 ? true : false);
			
			conn.disconnect();
		} catch (IOException e) {
			infor.setValid(false);
			StringBuilder sb = new StringBuilder("Error in detecting size of resource: ").append(infor.getUrl());
			logger.log(Level.SEVERE, sb.toString(), e);
		}
	}

}
