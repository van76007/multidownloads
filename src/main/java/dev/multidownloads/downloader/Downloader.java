package dev.multidownloads.downloader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class Downloader {
	final static Logger logger = LogManager.getLogger(Downloader.class);
	private static final int BUFFER_SIZE = 2048;
	DownloadListener progressListener;

	protected Segmentation seg;
	protected DownloadInfor infor;
	
	public Downloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		this.infor = infor;
		this.seg = seg;
		this.progressListener = progressListener;
		logger.info("To download a file {} in {}", infor.getFileName(), seg.toString());
	}
	
	public void transfer(InputStream input, RandomAccessFile raf, Segmentation seg) throws IOException {
		int bufSize = BUFFER_SIZE;
		try {
			bufSize = Integer.valueOf(Config.getProperty("BUFFER_SIZE"));
		} catch (NumberFormatException e) {
			logger.warn("No config of BUFFER_SIZE. To use the default value", e.getMessage());
		}
		
		int numByteRead;
		byte data[] = new byte[bufSize];
		
		int remaining = seg.endByte - seg.startByte + 1;
		while(remaining > 0) {
			int readBufSize = bufSize < remaining ? bufSize : remaining;
			numByteRead = input.read(data,0,readBufSize);
			
			raf.write(data,0,numByteRead);
			progressListener.onUpdate(numByteRead, infor.getFileName());
			
			seg.startByte += numByteRead;
			remaining -= numByteRead;
		}
		
		if(remaining < 0) {
			logger.error("Number of Remaining bytes is negative");
		}
	}
	
	protected void setError(Segmentation seg){}
}
