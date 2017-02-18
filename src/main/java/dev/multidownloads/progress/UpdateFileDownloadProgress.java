package dev.multidownloads.progress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateFileDownloadProgress implements DownloadListener {
	final static Logger logger = LogManager.getLogger(UpdateFileDownloadProgress.class);
	private int fileSize;
	private int totalCompleteBytes;
	
	public UpdateFileDownloadProgress(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int getTotalCompleteBytes() {
		return totalCompleteBytes;
	}

	public void setTotalCompleteBytes(int totalCompleteBytes) {
		this.totalCompleteBytes = totalCompleteBytes;
	}

	@Override
	public synchronized void onUpdate(int completeBytes, String fileName) {
		totalCompleteBytes += completeBytes;
		logger.info(String.format("Complete download %d bytes / total %d bytes of file %s\n", totalCompleteBytes,  this.fileSize, fileName));
	}
}
