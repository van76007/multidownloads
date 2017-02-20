package dev.multidownloads;

import java.util.Scanner;

/**
 * This application will download multiple URLs in parallel. For each URL, a
 * number of connections are opened in parallel if being supportted to download
 * Usage: java -cp multidownloads-0.0.1-SNAPSHOT.jar dev.multidownloads.MainApp
 * 
 * @author vanvu
 */
public class MainApp {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print(
				"Enter full path of the catalog file (Use ./ if the catalog file is in the same folder with the JAR file): \n");
		String catalogFileName = scanner.nextLine();

		boolean downloadResult = new DownloadManager().download(catalogFileName);
		System.out.println("Download result is:" + downloadResult);
		scanner.close();
	}
}
