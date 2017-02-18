package dev.multidownloads.prober;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;

public class FTPProber extends DownloadProber {
	final static Logger logger = LogManager.getLogger(FTPProber.class);
	private static final int TIMEOUT = 30000;

	@Override
	protected void inquiryFileLength(DownloadInfor infor) {
		try {
			URLConnection conn = new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.warn("No config of FTP connection TIMEOUT. To use the default value", e.getMessage());
			}
			conn.setConnectTimeout(timeout);
			int len = conn.getContentLength();
			
			infor.setFileLength(len);
			infor.setValid(len != -1 ? true : false);	
		} catch (IOException e) {
			logger.error("Error in detecting file length of resource: ", infor.getUrl(), e);
		}
	}

	@Override
	protected void inquiryIfSupportMultiPartsDownload(DownloadInfor infor) {
		try {
			String urlWithoutProtocol = infor.getUrl().substring(infor.getUrl().indexOf("://") + 3);
			FTPClient client = new FTPClient();
			client.connect(urlWithoutProtocol.substring(0, urlWithoutProtocol.indexOf("/")));
			
			logger.info("FTP Prober got reply: {}", client.getReplyString());
			
			infor.setSupportMultiPartsDownload(client.hasFeature("REST"));
			client.disconnect();
			
		} catch (IndexOutOfBoundsException | IOException e) {
			logger.error("Error in detecting if support multi parts download: {}", infor.getUrl(), e);
		}
	}
}
