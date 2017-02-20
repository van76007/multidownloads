package dev.multidownloads.progress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class updates the download percentage of a file
 * @author vanvu
 *
 */
public class UpdateFileDownloadProgress implements DownloadListener {
	final static Logger logger = LogManager.getLogger(UpdateFileDownloadProgress.class);
	
	/**
	 * Total file size
	 */
	private int fileSize;
	/**
	 * Number of complete downloaded bytes so far
	 */
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
