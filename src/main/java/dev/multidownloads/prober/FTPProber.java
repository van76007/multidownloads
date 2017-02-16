package dev.multidownloads.prober;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;

public class FTPProber extends DownloadProber {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 10000;

	@Override
	protected void inquiryFileLength(DownloadInfor infor) {
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

	@Override
	protected void inquiryIfSupportMultiPartsDownload(DownloadInfor infor) {
		try {
			String urlWithoutProtocol = infor.getUrl().substring(infor.getUrl().indexOf("://") + 3);
			FTPClient client = new FTPClient();
			client.connect(urlWithoutProtocol.substring(0, urlWithoutProtocol.indexOf("/")));
			
			System.out.println(client.getReplyString());
			logger.log(Level.FINE, "Prober: " + client.getReplyString());
			
			infor.setSupportMultiPartsDownload(client.hasFeature("REST"));
			client.disconnect();
			
		} catch (IndexOutOfBoundsException | IOException e) {
			StringBuilder sb = new StringBuilder("Error in detecting if support multi parts download: ").append(infor.getUrl());
			logger.log(Level.WARNING, sb.toString(), e);
		}
	}
}
