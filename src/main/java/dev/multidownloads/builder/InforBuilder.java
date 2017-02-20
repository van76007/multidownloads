package dev.multidownloads.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.factory.ProberFactory;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.prober.Prober;

/**
 * This class build a Download information, which gives detail about: Protocol, remote file name, remote file size
 * @author vanvu
 *
 */
public class InforBuilder {
	final static Logger logger = LogManager.getLogger(InforBuilder.class);
	
	/**
	 * A parser to extract protocol and file information from URL
	 */
	URLParser parser = new URLParser();
	
	/**
	 * This method build a list of Download informations from a list of URLs
	 * @param catalogLines List of URLs
	 * @param downloadDiretory Download directory associates with a download information
	 * @return List of download information
	 */
	public List<DownloadInfor> buildInfors(List<String> catalogLines, String downloadDiretory) {
		List<DownloadInfor> infors = new ArrayList<DownloadInfor>();
		for(String line : catalogLines) {
			DownloadInfor infor = buildInfor(line, downloadDiretory);
			if (infor.isValid())
				infors.add(infor);
		}
		return infors;
	}
	
	/**
	 * This method build a Download informations from an URL
	 * @param catalogLine URL.
	 * @param downloadDiretory Download directory associates with a download information
	 * @return A Download information
	 */
	private DownloadInfor buildInfor(String catalogLine, String downloadDirectory) {
		
		DownloadInfor infor = new DownloadInfor();
		infor.setDownloadDirectory(downloadDirectory);
		parser.setUrlAndDownloadCredentials(infor, catalogLine);
		
		if (!parser.setAndValidateProtocol(infor)) {
			return infor;
		}
		
		if (!parser.setAndValidateFileName(infor)) {
			return infor;
		}
		
		setFileSizeAndMultiPartsDownloadSupport(infor);
		return infor;
	}
	
	/**
	 * This method will populate the download information with Protocol and remote file size information
	 * @param infor
	 */
	private void setFileSizeAndMultiPartsDownloadSupport(DownloadInfor infor) {
		Prober prober = ProberFactory.getProbe(infor.getProtocol());
		prober.probeResource(infor);
	}
}
