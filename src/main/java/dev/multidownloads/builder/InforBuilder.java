package dev.multidownloads.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

import dev.multidownloads.factory.ProberFactory;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.Prober;

public class InforBuilder {
	private static final Logger logger = Logger.getLogger("dev.multidownloads");
	public static DownloadInfor buildInfor(String url, String downloadDirectory) {
		DownloadInfor infor = new DownloadInfor(url);
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
		if (Protocol.FTP == infor.getProtocol())
			infor.setSupportMultiPartsDownload(true);
		
		Prober prober = ProberFactory.getProbe(infor);
		prober.probeResource(infor);
	}
}
