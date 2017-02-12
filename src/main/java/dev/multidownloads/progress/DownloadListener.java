package dev.multidownloads.progress;

public interface DownloadListener {
	public void onUpdate(int completeParts, String jobName);
}
