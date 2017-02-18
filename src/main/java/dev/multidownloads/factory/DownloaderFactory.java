package dev.multidownloads.factory;

import java.util.concurrent.Callable;

import dev.multidownloads.downloader.FTPMultiPartsDownloader;
import dev.multidownloads.downloader.FTPSinglePartDownloader;
import dev.multidownloads.downloader.HTTPDownloader;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class DownloaderFactory {
	public static Callable<Segmentation> getDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		
		if (Protocol.HTTP == infor.getProtocol()) {
			return new HTTPDownloader(infor, seg, progressListener);
		}
		if (Protocol.FTP == infor.getProtocol() && !infor.isSupportMultiPartsDownload()) {
			return new FTPSinglePartDownloader(infor, seg, progressListener);
		}
		if (Protocol.FTP == infor.getProtocol() && infor.isSupportMultiPartsDownload()) {
			return new FTPMultiPartsDownloader(infor, seg, progressListener);
		}
		
		return null;
	}
}
