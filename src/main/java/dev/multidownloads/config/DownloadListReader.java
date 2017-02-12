package dev.multidownloads.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadListReader {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	
	public static List<String> readDownloadCatalog(String fileName) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading download catalog");
		}
		return lines;
	}
}
