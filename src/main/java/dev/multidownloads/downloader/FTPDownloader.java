package dev.multidownloads.downloader;
import java.io.BufferedInputStream;
import java.io.IOException;
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

public class FTPDownloader extends Downloader implements Callable<Segmentation> {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int TIMEOUT = 10000;
	
	public FTPDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}
	
	@Override
	public Segmentation call() throws Exception {
		BufferedInputStream in = null;
		RandomAccessFile raf = null;
		
		try {
			URLConnection conn = new URL(infor.getUrl()).openConnection();
			int timeout = TIMEOUT;
			try {
				timeout = Integer.valueOf(Config.getProperty("TIMEOUT"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "No config of FTP connection TIMEOUT");
			}
			conn.setConnectTimeout(timeout);
			
			// get the input stream and seek to the range of byte to download
			in = new BufferedInputStream(conn.getInputStream());
			in.skip(seg.getStartByte());
			
			// open the output file and seek to the start location
			StringBuilder sb = new StringBuilder(infor.getDownloadDirectory()).append(infor.getFileName());
			raf = new RandomAccessFile(sb.toString(), "rw");
			raf.seek(seg.getStartByte());
			
			// start transferring byte
			transfer(in, raf, this.seg);
			this.seg.setStatus(DownloadStatus.DONE);
		} catch (Exception e) {
			setError(this.seg);
			StringBuilder sb = new StringBuilder("Error in downloading 1 segment of file via FTP. Range: ").append(seg.getStartByte()).append("-").append(seg.getEndByte());
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
