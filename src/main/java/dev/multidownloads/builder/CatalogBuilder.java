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
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;

/**
 * This class build a download catalog which is composed of the download tasks
 * 
 * @author vanvu
 *
 */
public class CatalogBuilder {
	final static Logger logger = LogManager.getLogger(CatalogBuilder.class);
	private static final int SEGMENTATION_SIZE = 1048576; // 1048576 if want to split file into chunk of 1MB each. Or 262144 = 256kB
	
	/**
	 * A builder to build download information associated with each download task
	 */
	InforBuilder builder = new InforBuilder();
	/**
	 * A file reader to read the catalog file
	 */
	CatalogReader reader = new CatalogReader();
	
	public void setBuilder(InforBuilder builder) {
		this.builder = builder;
	}
	public void setReader(CatalogReader reader) {
		this.reader = reader;
	}
	
	/**
	 * This method populates the given download catalog with the download tasks.
	 * It also check if there is enough free disk space
	 * @param catalog Download catalog to be populated
	 * @param catalogFileName Catalog file name
	 */
	public void buildCatalog(DownloadCatalog catalog, String catalogFileName) {
		int segmentationSize = SEGMENTATION_SIZE;
		try {
			segmentationSize = Integer.valueOf(Config.getProperty("SEGMENTATION_SIZE"));
		} catch (NumberFormatException e) {
			logger.warn("No config of SEGMENTATION_SIZE. To use the default value {}", SEGMENTATION_SIZE);
		}
		
		String downloadDiretory = getDownloadDirectory();
		catalog.setValid(downloadDiretory != null);
		
		if (catalog.isValid()) {
			List<DownloadTask> tasks = buidTasks(downloadDiretory, catalogFileName, segmentationSize);
			catalog.setTasks(tasks);
			catalog.setValid(checkIfEnoughDiskspace(tasks, Paths.get(downloadDiretory)));
		}
	}
	
	/**
	 * This method retrieve the name of the Download directory from the config file.
	 * If there is no configuration, it will use the default folder %USER_HOME_DIRECTORY%\DL
	 * @return Path of the download directory
	 */
	public String getDownloadDirectory() {
		StringBuilder sb = new StringBuilder(System.getProperty("user.home")).append(File.separator).append("DL").append(File.separator);
		String defaultDownloadDirectory = sb.toString();
		String downloadDir = (Config.getProperty("DOWNLOAD_DIR") == null ? defaultDownloadDirectory : Config.getProperty("DOWNLOAD_DIR"));
		logger.info("Read DOWNLOAD_DIR is {}", downloadDir);
		
		File directory = new File(downloadDir);
		boolean existDirectory = directory.exists() && directory.isDirectory();
		
		if (!existDirectory) {
			try {
				existDirectory = directory.mkdirs();
			} catch (SecurityException e) {
				existDirectory = false;
				logger.error("Unable to download due to non-existing download directory {}", downloadDir, e);
			}
		}
		
		return (existDirectory ? downloadDir : null);
	}
	
	/**
	 * This method build a list of download tasks.
	 * Each task is associated with a download infor, which includes: Download directory, information about the remote file.
	 * Each task also includes a list of file segmentations to be retrieved.
	 * The list is sorted by file size so small file to be downloaded first in a queue.
	 * 
	 * @param downloadDiretory Name of download directory
	 * @param catalogFileName Name of catalog file from which URLs to be read
	 * @param segmentationSize
	 * @return list of tasks
	 */
	private List<DownloadTask> buidTasks(String downloadDiretory, String catalogFileName, int segmentationSize) {
		List<DownloadTask> tasks = new ArrayList<DownloadTask>();
		List<String> catalogLines = reader.readDownloadCatalog(catalogFileName);
		List<DownloadInfor> infors= builder.buildInfors(catalogLines, downloadDiretory);
		
		for (DownloadInfor infor : infors) {
			DownloadTask task = new DownloadTask(infor);
			if (infor.isSupportMultiPartsDownload()) {
				task.setSegmentations(buildSegmentations(infor.getFileLength(), segmentationSize));
			} else {
				task.setSegmentations(buildSegmentations(infor.getFileLength(), infor.getFileLength()));
			}
			tasks.add(task);
		}
		
		Collections.sort(tasks, new Comparator<DownloadTask>() {
			public int compare(DownloadTask t1, DownloadTask t2) {
				return (t1.getInfor().getFileLength() - t2.getInfor().getFileLength());
			}
		});
		
		return tasks;
	}
	
	/**
	 * This method split a file into segmentations with a given size
	 * The last segmentation will always end with the last byte of the file
	 * 
	 * @param fileSize Size of the file to be split
	 * @param segmentationSize Size of the segmentation
	 * @return list of segmentations
	 */
	private List<Segmentation> buildSegmentations(int fileSize, int segmentationSize) {
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
	
	/**
	 * This methid check if there is enough free disk space to download
	 * @param tasks List of download tasks
	 * @param parentDirectory Download directory
	 * @return
	 */
	private boolean checkIfEnoughDiskspace(List<DownloadTask> tasks, Path parentDirectory) {
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
}
