package dev.multidownloads.prober;

import dev.multidownloads.model.DownloadInfor;

public abstract class DownloadProber implements Prober {
	
	@Override
	public void probeResource(DownloadInfor infor) {
		inquiryIfSupportMultiPartsDownload(infor);
		inquiryFileLength(infor);
	}

	protected abstract void inquiryFileLength(DownloadInfor infor);
	protected abstract void inquiryIfSupportMultiPartsDownload(DownloadInfor infor);
}
