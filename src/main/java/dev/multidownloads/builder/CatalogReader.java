package dev.multidownloads.builder;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatalogReader {
	final static Logger logger = LogManager.getLogger(CatalogReader.class);
	
	public List<String> readDownloadCatalog(String fileName) {
		/*
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
		} catch (NullPointerException | IOException e) {
			logger.error("Error reading download catalog", e);
		}
		return lines;
		*/
		return Arrays.asList(
				"http://www.freeclassicebooks.com/Agatha%20Christie/The%20Secret%20Adversary.pdf",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.template;A;AA",
				"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
	}
}
