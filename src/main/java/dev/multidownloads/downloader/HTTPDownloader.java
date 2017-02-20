package dev.multidownloads.downloader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

/**
 * A class to download a file via HTTP protocol.
 * If the remote server understands the RANGE header, the file is split into multiple parts and they can be retrieved concurrently.
 * If the remote server does not support the RANGE header, this class downloads the whole file.
 * The class implements the Callable interface so it can be executed in a thread and return result after finish
 * @see Downloader
 * @author vanvu
 *
 */
public class HTTPDownloader extends Downloader implements Callable<Segmentation> {
	final static Logger logger = LogManager.getLogger(HTTPDownloader.class);
	private static final int TIMEOUT = 30000;
	
	public HTTPDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}
	
	/**
	 * This method download a file by HTTP protocol
	 */
	@Override
	public Segmentation call() throws Exception {
		BufferedInputStream in = null;
		RandomAccessFile raf = null;
		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.warn("No config of HTTP connection TIMEOUT. To use the default value {}", TIMEOUT);
			}
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			
			if (infor.isSupportMultiPartsDownload()) {
				// set the range of bytes to download
				StringBuilder sb = new StringBuilder("bytes=").append(seg.startByte).append("-").append(seg.endByte);
				conn.setRequestProperty("Range", sb.toString());
			}
			
			// connect to server
			conn.connect();
			// Continue only if the response code in range of 200
			if (conn.getResponseCode() / 100 != 2) {
				setError(this.seg);
			}
			
			seg.setStatus(DownloadStatus.DOWNLOADING);
			// get the input stream
			in = new BufferedInputStream(conn.getInputStream());
			
			// open the output file and seek to the start location
			StringBuilder sb = new StringBuilder(infor.getDownloadDirectory()).append(infor.getFileName());
			raf = new RandomAccessFile(sb.toString(), "rw");
			
			if (infor.isSupportMultiPartsDownload()) {
				raf.seek(seg.startByte);
			} else {
				// If single download, always download from the beginning of file
				raf.seek(0);
			}
			
			// start transferring bytes
			this.transfer(in, raf, this.seg);
			this.seg.setStatus(DownloadStatus.DONE);
		} catch (Exception e) {
			setError(this.seg);
			logger.error("Error in downloading 1 segment of file via HTTP. Range: {} - {}", seg.startByte, seg.endByte, e);
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
			
			conn.disconnect();
		}
		
		logger.info("Stop downloading seg: {}", seg.toString());
		return this.seg;
	}

	@Override
	public void setError(Segmentation seg) {
		seg.setStatus(DownloadStatus.ABORTED);
	}
}
