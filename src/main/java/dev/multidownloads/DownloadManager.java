package dev.multidownloads;

import java.io.File;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.builder.CatalogBuilder;
import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadStatus;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.progress.UpdateBatchDownloadProgress;

/**
 * Hello world!
 *
 */
public class DownloadManager 
{
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int NUM_OF_PARALLEL_DOWNLOAD = 2;
	private static final int MAX_NUM_OF_RETRY = 3;
	private static final int DELAY = 1000*180;
	
	public static void main(String[] args) {
		try{
			DownloadLogger.setup();
		} catch(IOException e) {}
		
		String catalogFileName = null;
		if (args.length > 0)
			catalogFileName = args[0];
		
		String now = new SimpleDateFormat("yyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		logger.log(Level.FINE, "Start downloading at " + now);
		
		DownloadCatalog catalog = new DownloadCatalog(catalogFileName);
		CatalogBuilder.buildCatalog(catalog);
		if (catalog.isValid()) {
			downloadWithRetry(catalog.getTasks());
		} else {
			logger.log(Level.SEVERE, "Impossible to download");
		}
		
		now = new SimpleDateFormat("yyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		logger.log(Level.FINE, "End downloading at " + now);
	}

	private static void downloadWithRetry(List<DownloadTask> tasks) {
		int maxRetry = MAX_NUM_OF_RETRY;
		int delay = DELAY;
		try {
			maxRetry = Integer.valueOf(Config.getProperty("MAX_NUM_OF_RETRY"));
			delay = Integer.valueOf(Config.getProperty("DELAY"));
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "No config of DELAY or RETRY");
		}
		
		int retry = 0;
		int numberOfCompleteFiles = 0;
		
		while(!tasks.isEmpty() && retry < maxRetry) {
			logger.log(Level.FINE, "Number of retry download: " + retry);
			if (retry > 0)
				try {  Thread.sleep(delay);  } catch (InterruptedException e) { e.printStackTrace(); }
			
			numberOfCompleteFiles = downloadOnePass(tasks, numberOfCompleteFiles);
			retry++;
		}
		
		if (!tasks.isEmpty()) {
			logger.log(Level.SEVERE, "Some downloads can not complete");
			clearCorruptedDownloadedFiles(tasks);
		} else {
			logger.log(Level.FINE, "All downloads finish");
		}
	}
	
	private static int downloadOnePass(List<DownloadTask> tasks, int numberOfCompleteFiles) {
		logger.log(Level.FINE, "Start one pass");
		int numberOfParallelDownload = NUM_OF_PARALLEL_DOWNLOAD;
		try {
			numberOfParallelDownload = Integer.valueOf(Config.getProperty("NUM_OF_PARALLEL_DOWNLOAD"));
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "No config of NUM_OF_PARALLEL_DOWNLOAD");
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(numberOfParallelDownload);
		List<Future<DownloadTask>> listOfDownloadTasks = new ArrayList<Future<DownloadTask>>();
		
		UpdateBatchDownloadProgress progress = new UpdateBatchDownloadProgress(tasks.size());
		progress.setNumberOfCompletedFiles(numberOfCompleteFiles);
		
		for (DownloadTask task : tasks) {
			StringBuilder sb = new StringBuilder("Submit a download task to queue. Infor: ").append(task.getInfor().toString());
			logger.log(Level.FINE, sb.toString());
			Callable<DownloadTask> downloadWorker = new DownloadWorker(task, progress);
			listOfDownloadTasks.add(executor.submit(downloadWorker));
		}
		
		for (Future<DownloadTask> future : listOfDownloadTasks) {
			try {
				DownloadTask task = future.get();
				if (DownloadStatus.DONE == task.getStatus())
					tasks.remove(task);
			} catch (InterruptedException | ExecutionException e) {
				logger.log(Level.SEVERE, "Error in executing download", e);
			}
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error in terminating download", e);
		}
		
		logger.log(Level.FINE, "End one pass");
		return progress.getNumberOfCompletedFiles();
	}
	
	private static void clearCorruptedDownloadedFiles(List<DownloadTask> tasks) {
		for (DownloadTask task : tasks) {
			StringBuilder sb = new StringBuilder(task.getInfor().getDownloadDirectory()).append(task.getInfor().getFileName());
			try {
				new File(sb.toString()).delete();
			} catch(Exception e) {
				logger.log(Level.SEVERE, "Error in deleting corrupted downloaded file", e);
			}
		}
	}
}
