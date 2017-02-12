package dev.multidownloads.downloader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class HTTPDownloader extends Downloader implements Callable<Segmentation> {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 10000;
	
	public HTTPDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}
	
	@Override
	public Segmentation call() throws Exception {
		BufferedInputStream in = null;
		RandomAccessFile raf = null;
		
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of HTTP connection TIMEOUT");
			}
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			
			if (infor.isSupportMultiPartsDownload()) {
				// set the range of bytes to download
				StringBuilder sb = new StringBuilder("bytes=").append(seg.getStartByte()).append("-").append(seg.getEndByte());
				conn.setRequestProperty("Range", sb.toString());
			}
			
			// connect to server
			conn.connect();
			// Continue only if the response code in range of 200
			if (conn.getResponseCode() / 100 != 2) {
				setError(this.seg);
			}
			
			// get the input stream
			in = new BufferedInputStream(conn.getInputStream());
			
			// open the output file and seek to the start location
			StringBuilder sb = new StringBuilder(infor.getDownloadDirectory()).append(infor.getFileName());
			raf = new RandomAccessFile(sb.toString(), "rw");
			
			if (infor.isSupportMultiPartsDownload()) {
				raf.seek(seg.getStartByte());
			} else {
				// If single download, always download from the beginning of file
				raf.seek(0);
			}
			
			// start transferring bytes
			this.transfer(in, raf, this.seg);
			this.seg.setStatus(DownloadStatus.DONE);
		} catch (Exception e) {
			setError(this.seg);
			StringBuilder sb = new StringBuilder("Error in downloading 1 segment of file via HTTP. Range: ").append(seg.getStartByte()).append("-").append(seg.getEndByte());
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
		}
		
		return this.seg;
	}

	@Override
	public void setError(Segmentation seg) {
		seg.setStatus(DownloadStatus.ABORTED);
	}
}
