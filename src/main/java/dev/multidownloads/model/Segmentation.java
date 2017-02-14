package dev.multidownloads.model;

public class Segmentation {
	private DownloadStatus status = DownloadStatus.QUEUED;
	public int startByte;
	public int endByte;
	
	public DownloadStatus getStatus() {
		return status;
	}
	public void setStatus(DownloadStatus status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "Segmentation [status=" + status + ", startByte=" + startByte + ", endByte=" + endByte + "]";
	}
}
