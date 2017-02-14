package dev.multidownloads.downloader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
		
		// HTTP read
		/*
		byte data[] = new byte[bufSize];
		int numByteRead;
		while((numByteRead = input.read(data,0,bufSize)) != -1 && seg.startByte < seg.endByte)
		{
			// write to buffer
			raf.write(data,0,numByteRead);
			// increase the startByte for retry later
			seg.startByte += numByteRead;
			// update progress
			progressListener.onUpdate(numByteRead, infor.getFileName());
		}
		*/
		
		// FTP read
		byte data[] = new byte[bufSize];
		int numByteRead;
		int remaining = seg.endByte - seg.startByte + 1;
		
		int readBufSize = bufSize < remaining ? bufSize : remaining;
		
		while((numByteRead = input.read(data,0,readBufSize)) != -1 && remaining > 0)
		{
			// write to buffer
			raf.write(data,0,numByteRead);			
			// increase the startByte for retry later
			seg.startByte += numByteRead;
			
			remaining -= numByteRead;
			readBufSize = bufSize < remaining ? bufSize : remaining;
			
			// update progress
			progressListener.onUpdate(numByteRead, infor.getFileName());
		}
		
		
	}
	
	protected void setError(Segmentation seg){}
}
