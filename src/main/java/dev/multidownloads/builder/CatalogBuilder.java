package dev.multidownloads.builder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;

public class CatalogBuilder {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	private static final int SEGMENTATION_SIZE = 4096 * 10;
	
	public static void buildCatalog(DownloadCatalog catalog) {
		int segmentationSize = SEGMENTATION_SIZE;
		try {
			segmentationSize = Integer.valueOf(Config.getProperty("SEGMENTATION_SIZE"));
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "No config of SEGMENTATION_SIZE");
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
		StringBuilder sb1 = new StringBuilder(System.getProperty("user.home")).append(File.separator).append("DL").append(File.separator);
		String defaultDownloadDirectory = sb1.toString();
		String downloadDir = (Config.getProperty("DOWNLOAD_DIR") == null ? defaultDownloadDirectory : Config.getProperty("DOWNLOAD_DIR"));
		
		File directory = new File(downloadDir);
		boolean existDirectory = directory.exists() && directory.isDirectory();
		
		if (!existDirectory) {
			try {
				existDirectory = directory.mkdirs();
			} catch (Exception e) {
				existDirectory = false;
				StringBuilder sb2 = new StringBuilder("Unable to download due to non-existing download directory").append(downloadDir);
				logger.log(Level.SEVERE, sb2.toString(), e);
			}
		}
		
		return (existDirectory ? downloadDir : null);
	}
	
	private static List<DownloadTask> buidTasks(String downloadDiretory, String catalogFileName, int segmentationSize) {
		List<DownloadTask> tasks = new ArrayList<DownloadTask>();
		
		for (String url : parseDownloadList(catalogFileName)) {
			DownloadInfor infor = InforBuilder.buildInfor(url, downloadDiretory);
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
			logger.log(Level.WARNING, "Impossible to determine free disk space", e);
		}
		return isEnoughSpace;
	}
	
	private static List<String> parseDownloadList(String downloadListFile) {
		//return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf");
		
		/*
		return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
				"http://www.freeclassicebooks.com/Louisa%20May%20Alcott/Short%20Stories/A%20Country%20Christmas.pdf");
		*/
		
		/*
		return Arrays.asList("ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/iso-cd/debian-8.7.1-amd64-CD-1.iso",
				"http://dl.my-film.org/reza/film/Acid%20480p%20DVDRip-[My-Film].mkv");
				*/
		/*
		return Arrays.asList("http://dl.my-film.org/reza/film/Coral.Reef.Adventure.2003.IMAX.720p-[My-Film].mkv",
				"http://dl.my-film.org/reza/film/Acid%20480p%20DVDRip-[My-Film].mkv");
		*/
		
		/*
		return Arrays.asList("https://www.w3.org/Protocols/HTTP/1.1/diff-v11-2068toRev02.doc",
				"https://www.w3.org/Protocols/HTTP/1.1/rfc2616.pdf");
				*/
		
		/*
		return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
				"http://www.freeclassicebooks.com/Louisa%20May%20Alcott/Short%20Stories/A%20Country%20Christmas.pdf",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/iso-cd/debian-8.7.1-amd64-CD-1.iso");
		*/
		
		/*
		return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
				"http://dl.my-film.org/reza/film/Coral.Reef.Adventure.2003.IMAX.720p-[My-Film].mkv");
		*/
		
		/*
		return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
				"http://dl.my-film.org/reza/film/Coral.Reef.Adventure.2003.IMAX.720p-[My-Film].mkv",
				"http://www.freeclassicebooks.com/Louisa%20May%20Alcott/Short%20Stories/A%20Country%20Christmas.pdf",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/iso-cd/debian-8.7.1-amd64-CD-1.iso");
		*/
		
		return Arrays.asList("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
				"http://www.freeclassicebooks.com/Louisa%20May%20Alcott/Short%20Stories/A%20Country%20Christmas.pdf",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.template",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
		
		//return DownloadListReader.readDownloadList(downloadListFile);
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
