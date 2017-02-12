package dev.multidownloads.progress;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateBatchDownloadProgress implements DownloadListener {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
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
		logger.log(Level.FINE, String.format("Complete download %d files / total %d files\n", numberOfCompletedFiles, this.totalNumberOfFiles));
	}
}
