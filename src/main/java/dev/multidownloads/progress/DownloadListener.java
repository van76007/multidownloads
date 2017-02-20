package dev.multidownloads.progress;

/**
 * This interface will be implemented by any lister wants to be updated with the
 * download progress
 * 
 * @author vanvu
 *
 */
public interface DownloadListener {
	public void onUpdate(int completeParts, String jobName);
}
