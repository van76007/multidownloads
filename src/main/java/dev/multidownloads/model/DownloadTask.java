package dev.multidownloads.model;
import java.util.List;

public class DownloadTask {
	private DownloadStatus status = DownloadStatus.QUEUED;
	private List<Segmentation> segmentations;
	private DownloadInfor infor;

	public DownloadTask(DownloadInfor infor) {
		this.infor = infor;
	}
	public DownloadInfor getInfor() {
		return infor;
	}
	public DownloadStatus getStatus() {
		return status;
	}
	public void setStatus(DownloadStatus status) {
		this.status = status;
	}
	public List<Segmentation> getSegmentations() {
		return segmentations;
	}
	public void setSegmentations(List<Segmentation> segmentations) {
		this.segmentations = segmentations;
	}
}
