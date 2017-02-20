package dev.multidownloads.prober;

import dev.multidownloads.model.DownloadInfor;

/**
 * This interface will be implemented by any prober to populate a download information by the information about remote server
 * @author vanvu
 *
 */
public interface Prober {
	public void probeResource(DownloadInfor infor);
}
