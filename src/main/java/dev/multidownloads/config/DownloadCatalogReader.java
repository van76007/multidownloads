package dev.multidownloads.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DownloadCatalogReader {
	final static Logger logger = LogManager.getLogger(DownloadCatalogReader.class);
	
	public static List<String> readDownloadCatalog(String fileName) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
		} catch (IOException e) {
			logger.error("Error reading download catalog", e);
		}
		return lines;
	}
}
