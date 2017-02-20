package dev.multidownloads.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

/**
 * A class to download a file via FTP protocol if the server supports the REST command
 * The file is split into multiple parts and they can be retrieved concurrently.
 * This class only download 1 part of the file.
 * The class implements the Callable interface so it can be executed in a thread and return result after finish
 * @see Downloader
 * @author vanvu
 *
 */
public class FTPMultiPartsDownloader extends Downloader implements Callable<Segmentation> {
	final static Logger logger = LogManager.getLogger(FTPMultiPartsDownloader.class);
	private static final int TIMEOUT = 30000;
	private static final String DEFAULT_CREDENTIAL = "anonymous";
	
	public FTPMultiPartsDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}
	
	/**
	 * This method download a segmentation of file
	 */
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
				logger.warn("No config of FTP connection TIMEOUT. To use the default value {}", TIMEOUT);
			}
			client.setConnectTimeout(timeout);
			
			String urlWithoutProtocol = infor.getUrl().substring(infor.getUrl().indexOf("://") + 3);
			client.connect(urlWithoutProtocol.substring(0, urlWithoutProtocol.indexOf("/")));
			logger.info("FTP connect got reply {}", client.getReplyString());
			
			client.setSoTimeout(TIMEOUT);
			
			if (infor.getUserName() == null && infor.getPassword() == null) {
				client.login(DEFAULT_CREDENTIAL, DEFAULT_CREDENTIAL);
			} else {
				client.login(infor.getUserName(), infor.getPassword());
			}
			logger.debug("FTP loggin got reply {}", client.getReplyString());
			
			client.enterLocalPassiveMode();
			client.setFileType(FTP.BINARY_FILE_TYPE);
			logger.debug("Enter passive mode got reply {}", client.getReplyString());
			
			seg.setStatus(DownloadStatus.DOWNLOADING);
			
			client.setRestartOffset(seg.startByte);
			logger.debug("SetRestartOffset got reply {}", client.getReplyString());
			
			in = client.retrieveFileStream(urlWithoutProtocol.substring(urlWithoutProtocol.indexOf("/")));
			logger.debug("RetrieveFileStream {}", client.getReplyString());
			if(in == null) {
				throw new Exception("Can not retrieve remote resource");
			}
			
			// open the output file and seek to the start location
			StringBuilder sb = new StringBuilder(infor.getDownloadDirectory()).append(infor.getFileName());
			raf = new RandomAccessFile(sb.toString(), "rw");
			raf.seek(seg.startByte);
			
			// start transferring byte
			transfer(in, raf, this.seg);
			this.seg.setStatus(DownloadStatus.DONE);
		} catch (Exception e) {
			setError(this.seg);
			logger.error("Error in downloading 1 segment of file via FTP. Range: {} - {}. Error: {}", seg.startByte, seg.endByte, e.getMessage());
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
		
		logger.info("Stop downloading seg {}", seg.toString());
		return this.seg;
	}
	
	@Override
	public void setError(Segmentation seg) {
		seg.setStatus(DownloadStatus.ABORTED);
	}

}
