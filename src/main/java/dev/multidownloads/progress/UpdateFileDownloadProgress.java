package dev.multidownloads.progress;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateFileDownloadProgress implements DownloadListener {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
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
		logger.log(Level.FINE, String.format("Complete download %d bytes / total %d bytes of file %s\n", totalCompleteBytes,  this.fileSize, fileName));
	}
}
