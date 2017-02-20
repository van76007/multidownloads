package dev.multidownloads.model;

/**
 * This model a part of file to be retrieved concurrently
 * 
 * @author vanvu
 *
 */
public class Segmentation {
	private DownloadStatus status = DownloadStatus.QUEUED;
	// Start of the file segmentation
	public int startByte;
	// End of the file segmentation
	public int endByte;

	public Segmentation() {
	}

	public Segmentation(int startByte, int endByte) {
		this.startByte = startByte;
		this.endByte = endByte;
	}

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
