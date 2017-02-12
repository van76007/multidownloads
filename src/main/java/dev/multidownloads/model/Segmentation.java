package dev.multidownloads.model;

public class Segmentation {
	private DownloadStatus status = DownloadStatus.QUEUED;
	private int startByte;
	private int endByte;
	
	public DownloadStatus getStatus() {
		return status;
	}
	public void setStatus(DownloadStatus status) {
		this.status = status;
	}
	public int getStartByte() {
		return startByte;
	}
	public void setStartByte(int startByte) {
		this.startByte = startByte;
	}
	public int getEndByte() {
		return endByte;
	}
	public void setEndByte(int endByte) {
		this.endByte = endByte;
	}
	
	@Override
	public String toString() {
		return "Segmentation [status=" + status + ", startByte=" + startByte + ", endByte=" + endByte + "]";
	}
}
