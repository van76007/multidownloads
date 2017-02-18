package dev.multidownloads.builder;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;

public class URLParser {
	final static Logger logger = LogManager.getLogger(URLParser.class);
	private static String SEPARATOR = ";";
	
	public void setAndValidateUrl(DownloadInfor infor, String catalogLine) {
		String[] tokens = getUrlAndCredentials(catalogLine);
		
		if (tokens.length > 0) {
			infor.setUrl(tokens[0]);
		}
		if (tokens.length > 1) {
			infor.setUserName(tokens[1]);
		}
		if (tokens.length > 2) {
			infor.setPassword(tokens[2]);
		}
	}

	public boolean setAndValidateProtocol(DownloadInfor infor) {
		Protocol p = getProtocol(infor.getUrl());
		infor.setProtocol(p);
		infor.setValid(p == null ? false:true);
		return infor.isValid();
	}
	
	public boolean setAndValidateFileName(DownloadInfor infor) {
		try {
			String fileName = getFileName(infor.getUrl());
			infor.setFileName(fileName);
			infor.setValid(fileName.indexOf(".") != -1);
		} catch (IndexOutOfBoundsException e) {
			infor.setValid(false);
			logger.error("Impossible to determine file name", e);
		}
		return infor.isValid();
	}
	
	private String[] getUrlAndCredentials(String catalogLine) {
		String separator = Config.getProperty("SEPARATOR") != null ? Config.getProperty("SEPARATOR") : SEPARATOR;
		StringTokenizer st = new StringTokenizer(catalogLine, separator);
		ArrayList<String> arr = new ArrayList<String>(catalogLine.length());
		while(st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		return arr.toArray(new String[0]);
	}
	
	private Protocol getProtocol(String url) {
		String proto = null;
		try {
			proto = url.substring(0, url.indexOf("://"));
		} catch (IndexOutOfBoundsException  e) {
			logger.error("Impossible to determine download protocol", e);
		}
		return Protocol.getEnum(proto);
	}
	
	private String getFileName(String url) throws IndexOutOfBoundsException {
		return url.substring(url.lastIndexOf('/')+1, url.length());
	}
}
