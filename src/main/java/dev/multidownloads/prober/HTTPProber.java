package dev.multidownloads.prober;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;

/**
 * This class probes the HTTP server
 * @author vanvu
 *
 */
public class HTTPProber extends DownloadProber {
	final static Logger logger = LogManager.getLogger(HTTPProber.class);
	private static final int TIMEOUT = 30000;
	
	protected void inquiryIfSupportMultiPartsDownload(DownloadInfor infor) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.warn("No config of HTTP connection TIMEOUT. To use the default value {}", TIMEOUT);
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
			logger.error("Error in detecting if server support multi-part download of of resource: {}", infor.getUrl(), e);
		}
	}
	
	protected void inquiryFileLength(DownloadInfor infor) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.warn("No config of HTTP connection TIMEOUT. To use the default value", e.getMessage());
			}
			conn.setConnectTimeout(timeout);
			conn.connect();
			
			// Continue only if the response code in range of 200
			if (conn.getResponseCode() / 100 != 2) {
				logger.error("Resource not found {}", infor.getUrl());
				infor.setValid(false);
			} else {
				int len = conn.getContentLength();
				infor.setFileLength(len);
				infor.setValid(len != -1 ? true : false);
			}
			
			conn.disconnect();
		} catch (IOException e) {
			infor.setValid(false);
			logger.error("Error in detecting size of resource: {}", infor.getUrl(), e);
		}
	}
}
