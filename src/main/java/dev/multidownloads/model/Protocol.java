package dev.multidownloads.model;

/**
 * This is the list of current supported download protocol
 * @author vanvu
 * @ToDo: Add HTTPS, SFTP
 */
public enum Protocol {
	HTTP("http"), 
	FTP("ftp");
	
	private String proto;
	
	Protocol(String proto) {
		this.proto = proto;
	}
	
	public String proto() {
		return proto;
	}
	
	public static Protocol getEnum(String proto) {
		switch(proto) {
			case "http":
				return HTTP;
			case "ftp":
				return FTP;
			default:
				return null;
		}
	}
}
