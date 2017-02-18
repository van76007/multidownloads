package dev.multidownloads.progress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateBatchDownloadProgress implements DownloadListener {
	final static Logger logger = LogManager.getLogger(UpdateBatchDownloadProgress.class);
	private int totalNumberOfFiles;
	private int numberOfCompletedFiles;

	public UpdateBatchDownloadProgress(int totalNumberOfFiles) {
		this.totalNumberOfFiles = totalNumberOfFiles;
	}
	
	public int getNumberOfCompletedFiles() {
		return numberOfCompletedFiles;
	}

	public void setNumberOfCompletedFiles(int numberOfCompletedFiles) {
		this.numberOfCompletedFiles = numberOfCompletedFiles;
	}
	
	@Override
	public synchronized void onUpdate(int completeFiles, String unused) {
		numberOfCompletedFiles += completeFiles;
		logger.info(String.format("Complete download %d files / total %d files\n", numberOfCompletedFiles, this.totalNumberOfFiles));
	}
}
