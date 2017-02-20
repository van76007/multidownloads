package dev.multidownloads.model;

/**
 * This models the download information associated with an URL
 * @author vanvu
 *
 */
public class DownloadInfor {

	private String url = "";
	private Protocol protocol;
	private String downloadDirectory;
	private String fileName;
	private String userName;
	private String password;
	private int fileLength;
	private int totalCompleteBytes;
	private boolean supportMultiPartsDownload;
	private boolean valid;
	
	public DownloadInfor() {
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Protocol getProtocol() {
		return protocol;
	}
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getFileLength() {
		return fileLength;
	}
	public void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}
	public int getTotalCompleteBytes() {
		return totalCompleteBytes;
	}
	public void setTotalCompleteBytes(int totalCompleteBytes) {
		this.totalCompleteBytes = totalCompleteBytes;
	}
	public boolean isSupportMultiPartsDownload() {
		return supportMultiPartsDownload;
	}
	public void setSupportMultiPartsDownload(boolean supportMultiPartsDownload) {
		this.supportMultiPartsDownload = supportMultiPartsDownload;
	}
	
	@Override
	public String toString() {
		return "DownloadInfor [url=" + url + ", protocol=" + protocol + ", downloadDirectory=" + downloadDirectory
				+ ", fileName=" + fileName + ", fileLength=" + fileLength + ", totalCompleteBytes=" + totalCompleteBytes
				+ ", supportMultiPartsDownload=" + supportMultiPartsDownload + "]";
	}
}
