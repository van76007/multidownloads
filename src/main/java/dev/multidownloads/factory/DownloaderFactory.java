package dev.multidownloads.factory;

import java.util.concurrent.Callable;

import dev.multidownloads.downloader.FTPMultiPartsDownloader;
import dev.multidownloads.downloader.FTPSinglePartDownloader;
import dev.multidownloads.downloader.HTTPDownloader;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;
import dev.multidownloads.progress.SpeedInformer;

/**
 * This class return different network downloader depending on the download
 * protocol. Any future supported protocol will require an ad-on to this class
 * 
 * @author vanvu
 *
 */
public class DownloaderFactory {
	/**
	 * This method returns different types of the network downloader
	 * 
	 * @param infor
	 *            Download infor to initialize the network downloader
	 * @param seg
	 *            Segmentation to initialize the network downloader
	 * @param progressListener
	 *            Listener to update download progress
	 * @return A network downloader
	 */
	public static Callable<Segmentation> getDownloader(DownloadInfor infor, Segmentation seg,
			DownloadListener progressListener, SpeedInformer speedInformer) {

		if (Protocol.HTTP == infor.getProtocol()) {
			return new HTTPDownloader(infor, seg, progressListener, speedInformer);
		}
		if (Protocol.FTP == infor.getProtocol() && !infor.isSupportMultiPartsDownload()) {
			return new FTPSinglePartDownloader(infor, seg, progressListener, speedInformer);
		}
		if (Protocol.FTP == infor.getProtocol() && infor.isSupportMultiPartsDownload()) {
			return new FTPMultiPartsDownloader(infor, seg, progressListener, speedInformer);
		}

		return null;
	}
}
