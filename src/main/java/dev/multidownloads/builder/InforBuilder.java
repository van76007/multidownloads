package dev.multidownloads.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.factory.ProberFactory;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.prober.Prober;

public class InforBuilder {
	final static Logger logger = LogManager.getLogger(InforBuilder.class);
	
	URLParser parser = new URLParser();
	
	public List<DownloadInfor> buildInfors(List<String> catalogLines, String downloadDiretory) {
		List<DownloadInfor> infors = new ArrayList<DownloadInfor>();
		for(String line : catalogLines) {
			DownloadInfor infor = buildInfor(line, downloadDiretory);
			if (infor.isValid())
				infors.add(infor);
		}
		return infors;
	}
	
	private DownloadInfor buildInfor(String catalogLine, String downloadDirectory) {
		
		DownloadInfor infor = new DownloadInfor();
		infor.setDownloadDirectory(downloadDirectory);
		parser.setAndValidateUrl(infor, catalogLine);
		
		if (!parser.setAndValidateProtocol(infor)) {
			return infor;
		}
		
		if (!parser.setAndValidateFileName(infor)) {
			return infor;
		}
		
		setFileSizeAndMultiPartsDownloadSupport(infor);
		return infor;
	}
	
	private void setFileSizeAndMultiPartsDownloadSupport(DownloadInfor infor) {
		Prober prober = ProberFactory.getProbe(infor.getProtocol());
		prober.probeResource(infor);
	}
}
