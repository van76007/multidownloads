package dev.multidownloads.builder;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.factory.ProberFactory;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.Prober;

public class InforBuilder {
	private static String SEPARATOR = ";";
	
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	public static DownloadInfor buildInfor(String catalogLine, String downloadDirectory) {
		
		DownloadInfor infor = new DownloadInfor();
		if (!setAndValidateUrl(infor, catalogLine)) {
			return infor;
		}
		infor.setDownloadDirectory(downloadDirectory);
		
		if (!setAndValidateProtocol(infor)) {
			return infor;
		}
		
		if (!setAndValidateFileName(infor)) {
			return infor;
		}
		
		setFileSizeAndMultiPartsDownloadSupport(infor);
		return infor;
	}
	
	private static boolean setAndValidateUrl(DownloadInfor infor, String catalogLine) {
		String[] tokens = getUrlAndCredentials(catalogLine);
		infor.setValid(tokens.length > 0? true:false);
		if (tokens.length > 0) {
			infor.setUrl(tokens[0]);
		}
		if (tokens.length > 1) {
			infor.setUserName(tokens[1]);
		}
		if (tokens.length > 2) {
			infor.setPassword(tokens[2]);
		}
		return infor.isValid();
	}
	
	private static String[] getUrlAndCredentials(String catalogLine) {
		String separator = Config.getProperty("SEPARATOR") != null ? Config.getProperty("SEPARATOR") : SEPARATOR;
		StringTokenizer st = new StringTokenizer(catalogLine, separator);
		ArrayList<String> arr = new ArrayList<String>(catalogLine.length());
		while(st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		return arr.toArray(new String[0]);
	}

	private static boolean setAndValidateProtocol(DownloadInfor infor) {
		Protocol p = getProtocol(infor.getUrl());
		infor.setProtocol(p);
		infor.setValid(p == null ? false:true);
		return infor.isValid();
	}
	
	private static Protocol getProtocol(String url) {
		String proto = null;
		try {
			proto = url.substring(0, url.indexOf("://"));
		} catch (IndexOutOfBoundsException  e) {
			logger.log(Level.SEVERE, "Impossible to determine protocol", e);
		}
		return Protocol.getEnum(proto);
	}
	
	private static boolean setAndValidateFileName(DownloadInfor infor) {
		try {
			String fileName = getFileName(infor.getUrl());
			infor.setFileName(fileName);
			infor.setValid(fileName.indexOf(".") != -1);
		} catch (IndexOutOfBoundsException e) {
			infor.setValid(false);
			logger.log(Level.SEVERE, "Impossible to determine file name", e);
		}
		return infor.isValid();
	}
	
	private static String getFileName(String url) throws IndexOutOfBoundsException {
		return url.substring(url.lastIndexOf('/')+1, url.length());
	}
	
	private static void setFileSizeAndMultiPartsDownloadSupport(DownloadInfor infor) {
		Prober prober = ProberFactory.getProbe(infor);
		prober.probeResource(infor);
	}
}
