package test.dev.multidownloads.downloader;

import dev.multidownloads.downloader.Downloader;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

public class MockDownloader extends Downloader {

	public MockDownloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		super(infor, seg, progressListener);
	}

	@Override
	protected void setError(Segmentation seg) {
	}
}
