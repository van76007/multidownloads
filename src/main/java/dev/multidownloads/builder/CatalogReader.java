package dev.multidownloads.builder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class read a catalog file and build list of URLs
 * @author vanvu
 *
 */
public class CatalogReader {
	final static Logger logger = LogManager.getLogger(CatalogReader.class);
	
	/**
	 * This method parses a text file line by line
	 * @param fileName Catalog file name
	 * @return Collection of lines in this catalog file
	 */
	public List<String> readDownloadCatalog(String fileName) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
		} catch (FileNotFoundException e) {
			logger.warn("Catalog file not found");
		} catch (IOException e) {
			logger.error("Error reading download catalog", e);
		}
		return lines;
	}
}
