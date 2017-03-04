package test.dev.multidownloads.downloader;

import dev.multidownloads.downloader.Downloader;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;
import dev.multidownloads.progress.SpeedInformer;

public class MockDownloader extends Downloader {

	public MockDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener, SpeedInformer speedInformer) {
		super(infor, seg, progressListener, speedInformer);
	}

	@Override
	protected void setError(Segmentation seg) {
	}
}
