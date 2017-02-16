package dev.multidownloads.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class FTPMultiPartsDownloader extends Downloader implements Callable<Segmentation> {
	
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 30000;
	private static final String DEFAULT_CREDENTIAL = "anonymous";
	
	public FTPMultiPartsDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}

	@Override
	public Segmentation call() throws Exception {
		
		InputStream in = null;
		RandomAccessFile raf = null;
		FTPClient client = new FTPClient();
		
		try {
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of FTP connection TIMEOUT");
			}
			client.setConnectTimeout(timeout);
			
			String urlWithoutProtocol = infor.getUrl().substring(infor.getUrl().indexOf("://") + 3);
			client.connect(urlWithoutProtocol.substring(0, urlWithoutProtocol.indexOf("/")));
			logger.log(Level.FINE, "connect: " + client.getReplyString());
			
			client.setSoTimeout(TIMEOUT);
			
			if (infor.getUserName() == null && infor.getPassword() == null) {
				client.login(DEFAULT_CREDENTIAL, DEFAULT_CREDENTIAL);
			} else {
				client.login(infor.getUserName(), infor.getPassword());
			}
			logger.log(Level.FINE, "loggin: " + client.getReplyString());
			
			client.enterLocalPassiveMode();
			client.setFileType(FTP.BINARY_FILE_TYPE);
			logger.log(Level.FINE, "enter passive mode: " + client.getReplyString());
			
			seg.setStatus(DownloadStatus.DOWNLOADING);
			
			client.setRestartOffset(seg.startByte);
			logger.log(Level.FINE, "setRestartOffset: " + client.getReplyString());
			
			in = client.retrieveFileStream(urlWithoutProtocol.substring(urlWithoutProtocol.indexOf("/")));
			logger.log(Level.FINE, "retrieveFileStream: " + client.getReplyString());
			
			// open the output file and seek to the start location
			StringBuilder sb = new StringBuilder(infor.getDownloadDirectory()).append(infor.getFileName());
			raf = new RandomAccessFile(sb.toString(), "rw");
			raf.seek(seg.startByte);
			
			// start transferring byte
			transfer(in, raf, this.seg);
			this.seg.setStatus(DownloadStatus.DONE);
		} catch (Exception e) {
			setError(this.seg);
			StringBuilder sb = new StringBuilder("Error in downloading 1 segment of file via FTP. Range: ").append(seg.startByte).append("-").append(seg.endByte);
			logger.log(Level.SEVERE, sb.toString(), e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {}
			}
			
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			
			try {
				client.completePendingCommand();
			} catch (IOException e) {}
			
			if (client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				} catch (IOException e) {}
			}
		}
		
		StringBuilder sb = new StringBuilder("Stop downloading seg: ").append(seg.toString());
		logger.log(Level.FINE, sb.toString());
		return this.seg;
	}

}
