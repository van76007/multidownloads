package dev.multidownloads.builder;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;

/**
 * This class extract the protocol and file information from an URL to build a
 * Download information
 * 
 * @author vanvu
 *
 */
public class URLParser {
	final static Logger logger = LogManager.getLogger(URLParser.class);

	/**
	 * This method populate a download information with URL, username/password
	 * (if download requires credentials)
	 * 
	 * @param infor
	 *            The download information to be populated
	 * @param catalogLine
	 *            The URL line in the catalog file
	 */
	public void setUrlAndDownloadCredentials(DownloadInfor infor, String catalogLine) {
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

	/**
	 * This method populates a download information with the download protocol
	 * 
	 * @param infor
	 *            The download information to be populated
	 * @return False if the protocol is unknown and true otherwise
	 */
	public boolean setAndValidateProtocol(DownloadInfor infor) {
		Protocol p = getProtocol(infor.getUrl());
		infor.setProtocol(p);
		infor.setValid(p == null ? false : true);
		return infor.isValid();
	}

	/**
	 * This method populates a download information with the remote file name
	 * 
	 * @param infor
	 *            The download information to be populated
	 * @return True if the file name contains dot and false otherwise
	 */
	public boolean setAndValidateFileName(DownloadInfor infor) {
		try {
			String fileName = getFileName(infor.getUrl());
			infor.setFileName(fileName);
			// A naive assumption.
			// @ToDo: Validate by known File extension.
			infor.setValid(fileName.indexOf(".") != -1);
		} catch (IndexOutOfBoundsException e) {
			infor.setValid(false);
			logger.error("Impossible to determine file name", e);
		}
		return infor.isValid();
	}

	/**
	 * This method parses a catalog lines to extract (URL, username, password)
	 * 
	 * @param catalogLine
	 * @return Tuplet (URL, username, password)
	 */
	private String[] getUrlAndCredentials(String catalogLine) {
		String separator = Config.getParameterAsString("SEPARATOR");
		StringTokenizer st = new StringTokenizer(catalogLine, separator);
		ArrayList<String> arr = new ArrayList<String>(catalogLine.length());
		while (st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		return arr.toArray(new String[0]);
	}

	/**
	 * This method extracts the protocol string from the prefix of an URL
	 * 
	 * @param url
	 *            An URL
	 * @return A protocol
	 */
	private Protocol getProtocol(String url) {
		String proto = null;
		try {
			proto = url.substring(0, url.indexOf("://"));
		} catch (IndexOutOfBoundsException e) {
			logger.error("Impossible to determine download protocol", e);
		}
		return Protocol.getEnum(proto);
	}

	/**
	 * This method extracts the file name from the suffix of an URL
	 * 
	 * @param url
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	private String getFileName(String url) throws IndexOutOfBoundsException {
		return url.substring(url.lastIndexOf('/') + 1, url.length());
	}
}
