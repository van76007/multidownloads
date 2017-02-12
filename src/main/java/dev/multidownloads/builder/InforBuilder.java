package dev.multidownloads.builder;

import dev.multidownloads.factory.ProberFactory;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.Prober;

public class InforBuilder {
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
		String proto = url.substring(0, url.indexOf("://"));
		return Protocol.getEnum(proto);
	}
	
	private static boolean setAndValidateFileName(DownloadInfor infor) {
		String fileName = getFileName(infor.getUrl());
		infor.setFileName(fileName);
		infor.setValid(fileName.indexOf(".") != -1);
		return infor.isValid();
	}
	
	private static String getFileName(String url) {
		return url.substring(url.lastIndexOf('/')+1, url.length());
	}
	
	private static void setFileSizeAndMultiPartsDownloadSupport(DownloadInfor infor) {
		if (Protocol.FTP == infor.getProtocol())
			infor.setSupportMultiPartsDownload(true);
		
		Prober prober = ProberFactory.getProbe(infor);
		prober.probeResource(infor);
	}
}
