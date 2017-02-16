package dev.multidownloads;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.factory.DownloaderFactory;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;
import dev.multidownloads.progress.UpdateFileDownloadProgress;

public class DownloadWorker implements Callable<DownloadTask> {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int NUM_OF_CONCURRENT_CONNECTION = 5;
	private static final int TIMEOUT_IN_SECONDS = 30;
	
	DownloadTask task;
	DownloadListener progressListener;

	public DownloadWorker(DownloadTask task, DownloadListener progressListener) {
		this.task = task;
		this.progressListener = progressListener;
	}

	@Override
	public DownloadTask call() throws Exception {
		int numOfConcurrentConnections = 1;
		if (task.getInfor().isSupportMultiPartsDownload()) {
			try {
				numOfConcurrentConnections = Integer.valueOf(Config.getProperty("NUM_OF_CONCURRENT_CONNECTION"));
			} catch (NumberFormatException e) {
				numOfConcurrentConnections = NUM_OF_CONCURRENT_CONNECTION;
				logger.log(Level.WARNING, "No config of NUM_OF_CONCURRENT_CONNECTION");
			}
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(numOfConcurrentConnections);
		List<Future<Segmentation>> listOfConcurrentConnections = new ArrayList<Future<Segmentation>>();
		
		UpdateFileDownloadProgress fileDownloadProgress = new UpdateFileDownloadProgress(task.getInfor().getFileLength());
		fileDownloadProgress.setTotalCompleteBytes(task.getInfor().getTotalCompleteBytes());
		
		for (Segmentation seg : task.getSegmentations()) {
			Callable<Segmentation> connector = DownloaderFactory.getDownloader(task.getInfor(), seg, fileDownloadProgress);
			listOfConcurrentConnections.add(executor.submit(connector));
		}
		
		for (Future<Segmentation> future : listOfConcurrentConnections) {
			try {
				Segmentation seg = future.get();
				if(DownloadStatus.DONE == seg.getStatus()) {
					task.getSegmentations().remove(seg);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.log(Level.SEVERE, "Error in downloading some parts of file", e);
			}
		}
		
		if (task.getSegmentations().isEmpty()) {
			this.task.setStatus(DownloadStatus.DONE);
			this.progressListener.onUpdate(1, null);
		} else { 
			this.task.setStatus(DownloadStatus.ABORTED);
			this.task.getInfor().setTotalCompleteBytes(fileDownloadProgress.getTotalCompleteBytes());
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error in terminating download of 1 file", e);
		}
		
		StringBuilder sb = new StringBuilder("Stop retrieving resource: ").append(task.getInfor().getFileName());
		logger.log(Level.FINE, sb.toString());
		return this.task;
	}
}
