package dev.multidownloads.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the model of a download catalog built from a catalog file
 * @author vanvu
 *
 */
public class DownloadCatalog {
	private String catalogFileName;
	public void setCatalogFileName(String catalogFileName) {
		this.catalogFileName = catalogFileName;
	}
	public String getCatalogFileName() {
		return catalogFileName;
	}
	private boolean valid;
	
	/**
	 * List of download tasks, each is corresponding to a line in the catalog file
	 */
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
	@Override
	public String toString() {
		return "DownloadCatalog [catalogFileName=" + catalogFileName + ", valid=" + valid + ", tasks=" + tasks + "]";
	}
}
