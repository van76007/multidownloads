package dev.multidownloads.downloader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class FTPSinglePartDownloader extends Downloader implements Callable<Segmentation> {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 30000;
	
	public FTPSinglePartDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}
	
	@Override
	public Segmentation call() throws Exception {
		InputStream in = null;
		RandomAccessFile raf = null;
		
		try {
			URLConnection conn = new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of FTP connection TIMEOUT");
			}
			conn.setReadTimeout(timeout);
			conn.setConnectTimeout(timeout);
			
			seg.setStatus(DownloadStatus.DOWNLOADING);
			// get the input stream and seek to the range of byte to download
			in = new BufferedInputStream(conn.getInputStream());
			
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
		}
		
		StringBuilder sb = new StringBuilder("Stop downloading seg: ").append(seg.toString());
		logger.log(Level.FINE, sb.toString());
		return this.seg;
	}

	@Override
	public void setError(Segmentation seg) {
		seg.setStatus(DownloadStatus.ABORTED);
	}

}
