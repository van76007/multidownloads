package dev.multidownloads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.factory.DownloaderFactory;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;
import dev.multidownloads.progress.UpdateFileDownloadProgress;

/**
 * An implementation of the Callable interface, i.e. a process can be executed
 * independently in a thread and return some result. This class processes 1
 * download task. The file associated with the task is split into chunks. The
 * chunks are put into queue. A number of threads process the queue in parallel.
 * 
 * @see DownloadManager, Callable
 * @author vanvu
 *
 */
public class DownloadWorker implements Callable<DownloadTask> {
	final static Logger logger = LogManager.getLogger(DownloadWorker.class);
	private static final int NUM_OF_CONCURRENT_CONNECTION = 5;
	private static final int TIMEOUT_IN_SECONDS = 30;

	DownloadTask task;
	DownloadListener progressListener;

	/**
	 * Constructor
	 * 
	 * @param task
	 *            The download task
	 * @param progressListener
	 *            The listener will be updated with download progress, i.e.
	 *            number of the downloaded bytes / file size
	 */
	public DownloadWorker(DownloadTask task, DownloadListener progressListener) {
		this.task = task;
		this.progressListener = progressListener;
	}

	/**
	 * Execute the task in a thread
	 * 
	 * @return the task after downloading
	 */
	@Override
	public DownloadTask call() throws Exception {
		int numOfConcurrentConnections = 1;
		if (task.getInfor().isSupportMultiPartsDownload()) {
			try {
				numOfConcurrentConnections = Integer.valueOf(Config.getProperty("NUM_OF_CONCURRENT_CONNECTION"));
			} catch (NumberFormatException e) {
				numOfConcurrentConnections = NUM_OF_CONCURRENT_CONNECTION;
				logger.warn("No config of NUM_OF_CONCURRENT_CONNECTION. To use the default value {}",
						NUM_OF_CONCURRENT_CONNECTION);
			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(numOfConcurrentConnections);
		List<Future<Segmentation>> listOfConcurrentConnections = new ArrayList<Future<Segmentation>>();

		UpdateFileDownloadProgress fileDownloadProgress = new UpdateFileDownloadProgress(
				task.getInfor().getFileLength());
		fileDownloadProgress.setTotalCompleteBytes(task.getInfor().getTotalCompleteBytes());

		for (Segmentation seg : task.getSegmentations()) {
			Callable<Segmentation> connector = DownloaderFactory.getDownloader(task.getInfor(), seg,
					fileDownloadProgress);
			listOfConcurrentConnections.add(executor.submit(connector));
		}

		for (Future<Segmentation> future : listOfConcurrentConnections) {
			try {
				Segmentation seg = future.get();
				if (DownloadStatus.DONE == seg.getStatus()) {
					task.getSegmentations().remove(seg);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error in downloading some parts of file", e);
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
			logger.error("Error in terminating download one file", e);
		}

		logger.info("Stop retrieving resource {}", task.getInfor().getFileName());
		return this.task;
	}
}
