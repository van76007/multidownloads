package dev.multidownloads.downloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

/**
 * A class to download a file via FTP protocol if the server does not support
 * the REST command Therefore the remote file could not be split into multiple
 * parts retrieved concurrently. This class only download 1 big segmentation
 * which is the whole file. The class implements the Callable interface so it
 * can be executed in a thread and return result after finish
 * 
 * @see Downloader
 * @author vanvu
 *
 */
public class FTPSinglePartDownloader extends Downloader implements Callable<Segmentation> {
	final static Logger logger = LogManager.getLogger(FTPSinglePartDownloader.class);
	private static final int TIMEOUT = 30000;

	public FTPSinglePartDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}

	/**
	 * This method download a file by FTP protocol
	 */
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
				logger.warn("No config of FTP connection TIMEOUT. To use the default value {}", TIMEOUT);
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
			logger.error("Error in downloading 1 segment of file via FTP. Range: {} - {}", seg.startByte, seg.endByte,
					e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

		logger.info("Stop downloading seg: {}", seg.toString());
		return this.seg;
	}

	@Override
	public void setError(Segmentation seg) {
		seg.setStatus(DownloadStatus.ABORTED);
	}

}
