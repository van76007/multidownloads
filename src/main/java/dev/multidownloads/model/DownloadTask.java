package dev.multidownloads.model;

import java.util.List;

/**
 * This model a download task associated with a remote file resources
 * 
 * @author vanvu
 *
 */
public class DownloadTask {
	private DownloadStatus status = DownloadStatus.QUEUED;

	/**
	 * List of file parts to be retrieved from the remote file resource
	 */
	private List<Segmentation> segmentations;

	/**
	 * Download information associates wih the remote file resource
	 */
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
