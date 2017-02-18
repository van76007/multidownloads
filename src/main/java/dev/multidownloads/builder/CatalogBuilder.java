package dev.multidownloads.builder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.config.DownloadCatalogReader;
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;

public class CatalogBuilder {
	final static Logger logger = LogManager.getLogger(CatalogBuilder.class);
	private static final int SEGMENTATION_SIZE = 262144; // 256kB. Or 1048576 if want to split file into chunk of 1MB each
	
	public static void buildCatalog(DownloadCatalog catalog) {
		int segmentationSize = SEGMENTATION_SIZE;
		try {
			segmentationSize = Integer.valueOf(Config.getProperty("SEGMENTATION_SIZE"));
		} catch (NumberFormatException e) {
			logger.error("No config of SEGMENTATION_SIZE. To use the default value", e);
		}
		
		String downloadDiretory = getDownloadDirectory();
		catalog.setValid(downloadDiretory != null);
		
		if (catalog.isValid()) {
			List<DownloadTask> tasks = buidTasks(downloadDiretory, catalog.getCatalogFileName(), segmentationSize);
			catalog.setTasks(tasks);
			catalog.setValid(checkIfEnoughDiskspace(tasks, Paths.get(downloadDiretory)));
		}
	}
	
	private static String getDownloadDirectory() {
		StringBuilder sb = new StringBuilder(System.getProperty("user.home")).append(File.separator).append("DL").append(File.separator);
		String defaultDownloadDirectory = sb.toString();
		String downloadDir = (Config.getProperty("DOWNLOAD_DIR") == null ? defaultDownloadDirectory : Config.getProperty("DOWNLOAD_DIR"));
		
		File directory = new File(downloadDir);
		boolean existDirectory = directory.exists() && directory.isDirectory();
		
		if (!existDirectory) {
			try {
				existDirectory = directory.mkdirs();
			} catch (Exception e) {
				existDirectory = false;
				logger.error("Unable to download due to non-existing download directory {}", downloadDir, e);
			}
		}
		
		return (existDirectory ? downloadDir : null);
	}
	
	private static List<DownloadTask> buidTasks(String downloadDiretory, String catalogFileName, int segmentationSize) {
		List<DownloadTask> tasks = new ArrayList<DownloadTask>();
		
		for (String catalogLine : parseDownloadList(catalogFileName)) {
			DownloadInfor infor = InforBuilder.buildInfor(catalogLine, downloadDiretory);
			if (infor.isValid()) {
				DownloadTask task = new DownloadTask(infor);
				if (infor.isSupportMultiPartsDownload()) {
					task.setSegmentations(buildSegmentations(infor.getFileLength(), segmentationSize));
				} else {
					task.setSegmentations(buildSegmentations(infor.getFileLength(), infor.getFileLength()));
				}
				tasks.add(task);
			}
		}
		
		Collections.sort(tasks, new Comparator<DownloadTask>() {
			public int compare(DownloadTask t1, DownloadTask t2) {
				return (t1.getInfor().getFileLength() - t2.getInfor().getFileLength());
			}
		});
		
		return tasks;
	}
	
	private static boolean checkIfEnoughDiskspace(List<DownloadTask> tasks, Path parentDirectory) {
		boolean isEnoughSpace = true;
		Path root = parentDirectory.getRoot() == null ? parentDirectory : parentDirectory.getRoot();
		try {
			long freeSpace = root.toFile().getFreeSpace();
			long requiredSpace = 0L;

			for (DownloadTask task: tasks) {
				requiredSpace += task.getInfor().getFileLength();
				if (requiredSpace > freeSpace) {
					isEnoughSpace = false;
					break;
				}
			}
		} catch (SecurityException e) {
			logger.error("Impossible to determine free disk space", e);
		}
		return isEnoughSpace;
	}
	
	private static List<String> parseDownloadList(String downloadCatalogFile) {
		return DownloadCatalogReader.readDownloadCatalog(downloadCatalogFile);
	}
	
	private static List<Segmentation> buildSegmentations(int fileSize, int segmentationSize) {
		List<Segmentation> segs = new ArrayList<Segmentation>();
		
		int start = 0;
		while(start <= fileSize - 1) {
			Segmentation seg = new Segmentation();
			seg.startByte = start;
			start += segmentationSize;
			seg.endByte = (start < fileSize ? start - 1 : fileSize - 1);
			segs.add(seg);
		}
		
		return segs;
	}
}
