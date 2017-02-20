package dev.multidownloads.prober;

import dev.multidownloads.model.DownloadInfor;

/**
 * This is an abstract model of a network prober
 * @author vanvu
 *
 */
public abstract class DownloadProber implements Prober {
	
	/**
	 * This method populates download information by the size of remote file resource.
	 * It also detects if the server support multi-part download
	 */
	@Override
	public void probeResource(DownloadInfor infor) {
		inquiryIfSupportMultiPartsDownload(infor);
		inquiryFileLength(infor);
	}
	
	/**
	 * This method detects size of remote file
	 * @param infor A download information
	 */
	protected abstract void inquiryFileLength(DownloadInfor infor);
	/**
	 * This method detects if the server support multi-part download
	 * @param infor A download information
	 */
	protected abstract void inquiryIfSupportMultiPartsDownload(DownloadInfor infor);
}
