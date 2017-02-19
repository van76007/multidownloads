package dev.multidownloads;

public class MainApp 
{	
	public static void main(String[] args) {
		String catalogFileName = "";
		if (args.length > 0) {
			catalogFileName = args[0];
		}
		
		boolean downloadResult = new DownloadManager().download(catalogFileName);
		System.out.println("Download result is:" + downloadResult);
	}
}
