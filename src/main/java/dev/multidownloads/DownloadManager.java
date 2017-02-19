package dev.multidownloads;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.builder.CatalogBuilder;
import dev.multidownloads.builder.CatalogReader;
import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.progress.UpdateBatchDownloadProgress;

public class DownloadManager {
	final static Logger logger = LogManager.getLogger(DownloadManager.class);
	private static final int NUM_OF_PARALLEL_DOWNLOAD = 2;
	private static final int MAX_NUM_OF_RETRY = 2;
	private static final int DELAY = 1000*180;
	private static final int TIMEOUT_IN_SECONDS = 30;
	
	CatalogBuilder catalogBuilder = new CatalogBuilder();
	public CatalogBuilder getCatalogBuilder() {
		return catalogBuilder;
	}
	public void setCatalogBuilder(CatalogBuilder catalogBuilder) {
		this.catalogBuilder = catalogBuilder;
	}

	public boolean download(String catalogFileName) {
		boolean downloadResult = false;
		String now = new SimpleDateFormat("yyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		logger.info("Start downloading at {}", now);
		
		DownloadCatalog catalog = new DownloadCatalog();
		catalogBuilder.buildCatalog(catalog, catalogFileName);
		
		if (catalog.isValid()) {
			downloadResult = downloadWithRetry(catalog.getTasks());
		} else {
			logger.warn("Impossible to download");
		}
		
		now = new SimpleDateFormat("yyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		logger.info("End downloading at {}", now);
		return downloadResult;
	}
	
	private boolean downloadWithRetry(List<DownloadTask> tasks) {
		boolean downloadResult = false;
		int maxRetry = MAX_NUM_OF_RETRY;
		int delay = DELAY;
		try {
			maxRetry = Integer.valueOf(Config.getProperty("MAX_NUM_OF_RETRY"));
			delay = Integer.valueOf(Config.getProperty("DELAY"));
		} catch (NumberFormatException e) {
			logger.warn("No config of DELAY or RETRY. To use the default value. ", e.getMessage());
		}
		
		int retry = 0;
		int numberOfCompleteFiles = 0;
		
		while(!tasks.isEmpty() && retry < maxRetry) {
			logger.info("Number of retry download {}", retry);
			if (retry > 0)
				try {  Thread.sleep(delay);  } catch (InterruptedException e) { e.printStackTrace(); }
			
			numberOfCompleteFiles = downloadOnePass(tasks, numberOfCompleteFiles);
			retry++;
		}
		
		if (!tasks.isEmpty()) {
			logger.error("Some download tasks can not be completed");
			clearCorruptedFiles(tasks);
		} else {
			downloadResult = true;
			logger.info("All download tasks finish");
		}
		return downloadResult;
	}
	
	private int downloadOnePass(List<DownloadTask> tasks, int numberOfCompleteFiles) {
		logger.info("Start one download pass");
		int numberOfParallelDownload = NUM_OF_PARALLEL_DOWNLOAD;
		try {
			numberOfParallelDownload = Integer.valueOf(Config.getProperty("NUM_OF_PARALLEL_DOWNLOAD"));
		} catch (NumberFormatException e) {
			logger.warn("No config of NUM_OF_PARALLEL_DOWNLOAD. To use the default value", e.getMessage());
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(numberOfParallelDownload);
		List<Future<DownloadTask>> listOfDownloadTasks = new ArrayList<Future<DownloadTask>>();
		
		UpdateBatchDownloadProgress progress = new UpdateBatchDownloadProgress(tasks.size());
		progress.setNumberOfCompletedFiles(numberOfCompleteFiles);
		
		for (DownloadTask task : tasks) {
			logger.info("Submit a download task to queue. Infor {}", task.getInfor().toString());
			Callable<DownloadTask> downloadWorker = new DownloadWorker(task, progress);
			listOfDownloadTasks.add(executor.submit(downloadWorker));
		}
		
		for (Future<DownloadTask> future : listOfDownloadTasks) {
			try {
				DownloadTask task = future.get();
				if (DownloadStatus.DONE == task.getStatus())
					tasks.remove(task);
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Download error", e);
			}
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Download error", e);
		}
		
		logger.info("End one download pass");
		return progress.getNumberOfCompletedFiles();
	}
	
	private void clearCorruptedFiles(List<DownloadTask> tasks) {
		for (DownloadTask task : tasks) {
			StringBuilder sb = new StringBuilder(task.getInfor().getDownloadDirectory()).append(task.getInfor().getFileName());
			try {
				new File(sb.toString()).delete();
			} catch(Exception e) {
				logger.error("Error in deleting corrupted downloaded file", e);
			}
		}
	}
}
