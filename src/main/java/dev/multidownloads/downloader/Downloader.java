package dev.multidownloads.downloader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class Downloader {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int BUFFER_SIZE = 2048;
	DownloadListener progressListener;

	protected Segmentation seg;
	protected DownloadInfor infor;
	
	public Downloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		this.infor = infor;
		this.seg = seg;
		this.progressListener = progressListener;
		StringBuilder sb = new StringBuilder("To download a file ").append(infor.getFileName()).append(" in ").append(seg.toString());
		logger.log(Level.FINE, sb.toString());
	}
	
	protected void transfer(InputStream input, RandomAccessFile raf, Segmentation seg) throws IOException {
		int bufSize = BUFFER_SIZE;
		try {
			bufSize = Integer.valueOf(Config.getProperty("BUFFER_SIZE"));
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "No config of BUFFER_SIZE");
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
			logger.log(Level.SEVERE, "Number of Remaining bytes is negative");
		}
	}
	
	protected void setError(Segmentation seg){}
}
