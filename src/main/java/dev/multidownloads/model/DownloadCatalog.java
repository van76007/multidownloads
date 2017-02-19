package dev.multidownloads.model;

import java.util.ArrayList;
import java.util.List;

public class DownloadCatalog {
	private String catalogFileName;
	public void setCatalogFileName(String catalogFileName) {
		this.catalogFileName = catalogFileName;
	}
	public String getCatalogFileName() {
		return catalogFileName;
	}
	private boolean valid;
	private List<DownloadTask> tasks = new ArrayList<DownloadTask>();
	public List<DownloadTask> getTasks() {
		return tasks;
	}
	public void setTasks(List<DownloadTask> tasks) {
		this.tasks = tasks;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
}
